package it.communikein.bakingapp.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.bakingapp.ui.MainActivity;

@Module
public abstract class MainActivityModule {

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();

}
