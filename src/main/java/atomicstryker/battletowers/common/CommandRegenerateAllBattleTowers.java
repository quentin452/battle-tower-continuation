package atomicstryker.battletowers.common;

import net.minecraft.command.ICommandSender;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLCommonHandler;

public class CommandRegenerateAllBattleTowers extends CommandBattleTowers {

    @Override
    public String getCommandName() {
        return "regenerateallbattletowers";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "/regenerateallbattletowers re-spawns all Battletowers and their golems";
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] astring) {
        WorldGenHandler.deleteAllTowers(icommandsender.getEntityWorld(), true);
        FMLCommonHandler.instance()
            .getFMLLogger()
            .log(Level.INFO, icommandsender.getCommandSenderName() + ": Battletowers regenerated");
    }

}
