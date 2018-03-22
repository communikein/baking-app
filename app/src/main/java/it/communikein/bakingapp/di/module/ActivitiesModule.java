package it.communikein.bakingapp.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.bakingapp.ui.MainActivity;
import it.communikein.bakingapp.ui.RecipeDetailActivity;

@Module
public abstract class ActivitiesModule {

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector
    abstract RecipeDetailActivity contributeRecipeDetailActivity();

}
