package it.communikein.bakingapp.di.module;

import android.app.Application;
import android.arch.persistence.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import it.communikein.bakingapp.data.database.BakingDatabase;
import it.communikein.bakingapp.data.database.IngredientsDao;
import it.communikein.bakingapp.data.database.RecipesDao;
import it.communikein.bakingapp.data.database.StepsDao;

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

    @Singleton
    @Provides
    BakingDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application, BakingDatabase.class, BakingDatabase.NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton @Provides
    IngredientsDao provideIngredientsDao(BakingDatabase database) {
        return database.ingredientsDao();
    }

    @Singleton @Provides
    StepsDao provideStepsDao(BakingDatabase database) {
        return database.stepsDao();
    }

    @Singleton @Provides
    RecipesDao provideRecipesDao(BakingDatabase database) {
        return database.recipesDao();
    }

}
