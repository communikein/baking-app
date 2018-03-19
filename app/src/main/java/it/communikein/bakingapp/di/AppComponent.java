package it.communikein.bakingapp.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;
import it.communikein.bakingapp.BakingApp;
import it.communikein.bakingapp.di.module.BakingAppModule;
import it.communikein.bakingapp.di.module.MainActivityModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        BakingAppModule.class,
        MainActivityModule.class})
public interface AppComponent {

    void inject(BakingApp app);

    Application getApplication();

}