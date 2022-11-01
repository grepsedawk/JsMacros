package xyz.wagyourtail.jsmacros.client.api.classes.inventory;

import net.minecraft.client.gui.screen.ingame.VillagerTradingScreen;
import net.minecraft.village.TradeOffer;
import xyz.wagyourtail.jsmacros.client.access.IMerchantScreen;
import xyz.wagyourtail.jsmacros.client.api.helpers.world.entity.TradeOfferHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * @since 1.3.1
 */
@SuppressWarnings("unused")
public class VillagerInventory extends Inventory<VillagerTradingScreen> {
    
    protected VillagerInventory(VillagerTradingScreen inventory) {
        super(inventory);
    }
    
    /**
    *  select the trade by its index
    *
     * @param index
     *
     * @return self for chaining
     *
     * @since 1.3.1
     */
    public VillagerInventory selectTrade(int index) {
        ((IMerchantScreen)inventory).jsmacros_selectIndex(index);
        return this;
    }
    
    /**
     * @return
     * @since 1.3.1
     */
    public int getExperience() {
        return 0;
    }
    
    /**
     * @return
     * @since 1.3.1
     */
    public int getLevelProgress() {
        return 0;
    }
    
    /**
     * @return
     * @since 1.3.1
     */
    public int getMerchantRewardedExperience() {
        return 0;
    }
    
    /**
     * @return
     * @since 1.3.1
     */
    public boolean canRefreshTrades() {
        return false;
    }
    
    /**
     * @return
     * @since 1.3.1
     */
    public boolean isLeveled() {
        return false;
    }
    
    /**
     * @return list of trade offers
     * @since 1.3.1
     */
    public List<TradeOfferHelper> getTrades() {
        List<TradeOfferHelper> offers = new LinkedList<>();
        int i = -1;
        for (TradeOffer offer : inventory.getTrader().getOffers(mc.player)) {
            offers.add(new TradeOfferHelper(offer, ++i, this));
        }
        return offers;
    }

    @Override
    public String toString() {
        return String.format("VillagerInventory:{\"level\": %d}", getLevelProgress());
    }
    
}
