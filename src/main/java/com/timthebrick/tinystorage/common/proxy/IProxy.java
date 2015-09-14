package com.timthebrick.tinystorage.common.proxy;

public interface IProxy {

    ClientProxy getClientProxy ();

    void initRenderingAndTextures ();

    void registerEventHandlers ();

    void registerKeyBindings ();

    void playSound (String soundName, float xCoord, float yCoord, float zCoord, float volume, float pitch);

    String getMinecraftVersion ();

    void preInit();

    void init();

    void postInit();
}
