package codechicken.nei.jei;

import codechicken.lib.config.ConfigTagParent;
import codechicken.nei.VisibilityData;
import codechicken.nei.jei.proxy.JEIProxy;
import mezz.jei.Internal;
import mezz.jei.ItemFilter;
import mezz.jei.JeiRuntime;
import mezz.jei.config.Config;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.ItemListOverlayInternal;
import mezz.jei.input.GuiTextFieldFilter;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 7/14/2016.
 * Handles hiding and showing things from jei and nei. Basically an interface between the two.
 */
public class JEIIntegrationManager {

    public static final JEIProxy proxy = new JEIProxy();

    public static EnumItemBrowser searchBoxOwner = EnumItemBrowser.JEI;
    public static EnumItemBrowser recipeOwner = EnumItemBrowser.NEI;
    public static EnumItemBrowser itemPannelOwner = EnumItemBrowser.JEI;

    public static void pushChanges(VisibilityData data) {
        JeiRuntime runtime = Internal.getRuntime();
        ItemListOverlay overlay = runtime.getItemListOverlay();
        GuiTextFieldFilter fieldFilter = getTextFieldFilter(overlay.getInternal());

        if (searchBoxOwner == EnumItemBrowser.JEI) {
            data.showSearchSection = false;
            if (fieldFilter != null) {
                fieldFilter.setVisible(true);
            }
        } else {
            data.showSearchSection = true;
            if (fieldFilter != null) {
                fieldFilter.setVisible(false);
            }
        }
        if (itemPannelOwner == EnumItemBrowser.JEI) {
            data.showItemSection = false;
            if (!Config.isOverlayEnabled()) {
                Config.toggleOverlayEnabled();
            }
        } else {
            if (!data.showWidgets) {
                return;
            }
            data.showItemSection = true;
            if (Config.isOverlayEnabled()) {
                Config.toggleOverlayEnabled();
            }
        }

    }


    public static void initConfig(ConfigTagParent tag) {
        tag.removeTag("jei.panelOwner");

        setSearchBoxOwner(tag.getTag("jei.searchBoxOwner").getIntValue(0));

        //if (searchBoxOwner == EnumItemBrowser.JEI){
        //NEIClientConfig.setSearchExpression(Config.getFilterText(), false);
        //}
    }


    public static boolean setSearchBoxOwner(int ordinal) {
        try {
            searchBoxOwner = EnumItemBrowser.values()[ordinal];
            return true;
        } catch (IndexOutOfBoundsException e) {
            searchBoxOwner = EnumItemBrowser.NEI;
            return false;
        }
    }


    public static boolean setSearchBoxOwner(EnumItemBrowser browser) {
        searchBoxOwner = browser;
        return true;
    }


    public static GuiTextFieldFilter getTextFieldFilter() {
        if (Internal.getRuntime() == null || Internal.getRuntime().getItemListOverlay().getInternal() == null) {
            return null;
        }
        return getTextFieldFilter(Internal.getRuntime().getItemListOverlay().getInternal());
    }


    private static GuiTextFieldFilter getTextFieldFilter(ItemListOverlayInternal overlay) {
        try {
            Field field = overlay.getClass().getDeclaredField("searchField");
            field.setAccessible(true);
            return (GuiTextFieldFilter) field.get(overlay);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ItemFilter getItemFilter(GuiTextFieldFilter fieldFilter) {
        if (fieldFilter == null) {
            return null;
        }
        try {
            Field field = fieldFilter.getClass().getDeclaredField("itemFilter");
            field.setAccessible(true);
            return (ItemFilter) field.get(fieldFilter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFilterText(String text) {
        ItemListOverlay overlay = Internal.getRuntime().getItemListOverlay();
        overlay.setFilterText(text);
    }

    public static List<ItemStack> getFilteredItems() {
        ItemListOverlay overlay = Internal.getRuntime().getItemListOverlay();
        ItemFilter filter = getItemFilter(getTextFieldFilter(overlay.getInternal()));
        if (filter != null) {
            return filter.getItemStacks();
        }
        return new ArrayList<ItemStack>();
    }

}
