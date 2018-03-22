package it.communikein.bakingapp;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ContentProvider;
import android.content.Context;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasContentProviderInjector;
import dagger.android.HasServiceInjector;
import it.communikein.bakingapp.di.AppComponent;
import it.communikein.bakingapp.di.DaggerAppComponent;
import it.communikein.bakingapp.di.module.BakingAppModule;

public class BakingApp extends Application implements
        HasActivityInjector, HasServiceInjector, HasContentProviderInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidActivityInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingAndroidServiceInjector;

    @Inject
    DispatchingAndroidInjector<ContentProvider> dispatchingAndroidContentProviderInjector;

    AppComponent appComponent;

    public static BakingApp get(Context context) {
        return (BakingApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent
                .builder()
                .bakingAppModule(new BakingAppModule(this))
                .build();

        appComponent.inject(this);
    }

    public AppComponent getComponent() {
        return appComponent;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidServiceInjector;
    }

    @Override
    public AndroidInjector<ContentProvider> contentProviderInjector() {
        return dispatchingAndroidContentProviderInjector;
    }
}