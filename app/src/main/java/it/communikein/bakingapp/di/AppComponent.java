package it.communikein.bakingapp.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;
import it.communikein.bakingapp.BakingApp;
import it.communikein.bakingapp.di.module.BakingAppModule;
import it.communikein.bakingapp.di.module.BakingProviderModule;
import it.communikein.bakingapp.di.module.ActivitiesModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        BakingAppModule.class,
        ActivitiesModule.class,
        BakingProviderModule.class})
public interface AppComponent {
    void inject(BakingApp app);

    Application getApplication();
}