package it.communikein.bakingapp.di.module;

import android.app.Application;

import dagger.Module;
import dagger.Provides;

@Module
public class BakingAppModule {

    private final Application application;

    public BakingAppModule(Application application) {
        this.application = application;
    }

    @Provides
    Application provideApplication() {
        return application;
    }

}
