package com.demmodders.factions;

import com.demmodders.datmoddingapi.delayedexecution.DelayHandler;
import com.demmodders.factions.commands.CommandRegister;
import com.demmodders.factions.commands.FactionCommandList;
import com.demmodders.factions.delayedevents.FactionCleanout;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FlagDescriptions;
import com.demmodders.factions.util.structures.Version;
import com.google.gson.Gson;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Mod(modid = Factions.MODID, name = Factions.NAME, version = Factions.VERSION, acceptableRemoteVersions  = "*", dependencies = "required-after:datmoddingapi@[1.3.3,)")
public class Factions {
    public static final String MODID = "demfactions";
    public static final String NAME = "Factions";
    public static final String VERSION = "1.1.2";
    public static final String MC_VERSION = "[1.12.2]";
    public static final int COMMANDSVERSION = 2;
    public static final int FLAGSVERSION = 1;

    public static final Logger LOGGER = LogManager.getLogger(Factions.MODID);


    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Load faction data
        FactionManager.getInstance();
        File translationsDir = new File(event.getModConfigurationDirectory(), "/Dat Factions/Translations");
        if (!translationsDir.exists() || !translationsDir.isDirectory()) {
            if (!translationsDir.mkdir()) {
                LOGGER.error("Failed to create translations directory, command description and flag translations won't load");
                return;
            }
        }

        Gson gson = new Gson();
        Version fileVersion;

        try (InputStream file = getClass().getClassLoader().getResourceAsStream("JSON/Commands.json")) {
            File commandList = new File(translationsDir, "Commands.json");
            if (!commandList.exists()) {
                Files.copy(file, commandList.toPath());
                FactionCommandList.commandFile = commandList;
            } else {
                fileVersion = gson.fromJson(new FileReader(commandList), Version.class);
                if(fileVersion.version != COMMANDSVERSION) {
                    LOGGER.warn("Command List file out of date, updated to latest version, this means it will be cleared");
                    commandList.delete();
                    Files.copy(file, commandList.toPath());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create Commands translations file, command description and flag translations won't load");
            return;
        }

        try (InputStream file = getClass().getClassLoader().getResourceAsStream("JSON/Flags.json")) {
            File flagList = new File(translationsDir, "Flags.json");
            if (!flagList.exists()) {
                Files.copy(file, flagList.toPath());
                FlagDescriptions.flagFile = flagList;
            } else {
                fileVersion = gson.fromJson(new FileReader(flagList), Version.class);
                if(fileVersion.version != FLAGSVERSION) {
                    LOGGER.warn("Flag List file out of date, updated to latest version, this means it will be cleared");
                    flagList.delete();
                    Files.copy(file, flagList.toPath());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create flags translations file, command description and flag translations won't load");
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info(Factions.NAME + " says hi!");
        // Register permissions
        CommandRegister.RegisterPermissionNodes();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent e){
        // register commands
        CommandRegister.RegisterCommands(e);
        if (FactionConfig.factionSubCat.clearFactionPeriod != 0) DelayHandler.addEvent(new FactionCleanout());
    }
}
