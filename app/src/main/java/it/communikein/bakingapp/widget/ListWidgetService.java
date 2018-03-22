package it.communikein.bakingapp.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import it.communikein.bakingapp.R;
import it.communikein.bakingapp.data.model.Ingredient;

public class ListWidgetService extends RemoteViewsService {

    public static final String KEY_INGREDIENTS = "ingredients";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        List<Ingredient> ingredients = intent.getParcelableArrayListExtra(KEY_INGREDIENTS);
        return new ListRemoteViewsFactory(this.getApplicationContext(), ingredients);
    }


    class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        String mPackageName;
        List<Ingredient> mIngredients;

        ListRemoteViewsFactory(Context applicationContext, List<Ingredient> ingredients) {
            this.mPackageName = applicationContext.getPackageName();
            this.mIngredients = ingredients;
        }

        @Override
        public void onCreate() { }

        @Override
        public void onDataSetChanged() { }

        @Override
        public void onDestroy() { }

        @Override
        public int getCount() { return mIngredients.size(); }

        @Override
        public RemoteViews getViewAt(int position) {
            Ingredient ingredient = mIngredients.get(position);

            RemoteViews remoteViews = new RemoteViews(mPackageName, R.layout.list_item_ingredient);
            remoteViews.setTextViewText(R.id.name_textview, ingredient.getIngredient());

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }


}
