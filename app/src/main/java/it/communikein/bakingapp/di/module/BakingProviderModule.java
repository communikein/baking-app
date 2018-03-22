package it.communikein.bakingapp.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.bakingapp.data.contentprovider.BakingContentProvider;

@Module
public abstract class BakingProviderModule {

    @ContributesAndroidInjector
    abstract BakingContentProvider contributeContentProvider();

}
