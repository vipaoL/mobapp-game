// SPDX-License-Identifier: GPL-3.0-or-later

package utils;

import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.Utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author vipaol
 */
public class MgStruct {
    public static final int[] ARGS_NUMBER = {
            0,    // id0    EOF
            2,    // id1    END_POINT
            4,    // id2    LINE
            7,    // id3    CIRCLE
            9,    // id4    BROKEN_LINE
            10,   // id5    BROKEN_CIRCLE
            6,    // id6    SINE
            8,    // id7    ACCELERATOR
            6,    // id8    TRAMPOLINE
            2,    // id9    LEVEL_START
            5,    // id10   LEVEL_FINISH
            5,    // id11   LAVA
    };
    static final int STRUCTURE_STORAGE_SIZE = 32;

    public static short[][][] structStorage = new short[STRUCTURE_STORAGE_SIZE][][];
    public static int loadedStructsNumber = 0;
    static boolean isInited = false;
    public static int loadedFromResNumber = 0;
    public boolean loadCancelled = false;

    public MgStruct() {
        Logger.log("MGStruct constructor");
        if (!isInited) {
            Logger.log("mgs init");
            for (int i = 1; readRes("/s" + i + ".mgstruct"); i++) {
                Logger.log(i + ".mgstruct");
            }

            Logger.log("MGStruct:loaded " + loadedStructsNumber);
            loadedFromResNumber = loadedStructsNumber;
        }
        Logger.log("inited");
        isInited = true;
    }

    public boolean readRes(String path) {
        InputStream is = null;
        try {
            is = Platform.getResource(path);
            DataInputStream dis = new DataInputStream(is);
            try {
                saveStructToStorage(readFromDataInputStream(dis));
            } finally {
                try {
                    dis.close();
                } catch (IOException ignored) { }
            }
            return true;
        } catch (Exception ex) {
            Logger.log(path + " " + ex);
            return false;
        } finally {
            try {
                is.close();
            } catch (Exception ex) { }
        }
    }

    public boolean loadFromFiles() {
        Logger.log("mgs load()");
        loadCancelled = false;
        String[] paths = GameFileUtils.listFilesInAllPlaces("MobappGame/MGStructs");
        int loadedFromFiles = 0;
        loadedStructsNumber = loadedFromResNumber;
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            DataInputStream dis = null;
            try {
                dis = FileUtils.fileToDataInputStream(path);
            } catch (SecurityException sex) {
                Logger.log("mgs:load cancelled");
                sex.printStackTrace();
                loadCancelled = true;
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            } catch (NoClassDefFoundError err) {
                Platform.showError(err);
                return false;
            }
            if (dis != null) {
                try {
                    short[][] structure = readFromDataInputStream(dis);
                    if (structure != null) {
                        saveStructToStorage(structure);
                        loadedFromFiles += 1;
                        Logger.log(path + " loaded");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    dis.close();
                } catch (Exception ignored) { }
            }
        }
        if (loadedFromFiles > 0) {
            loadCancelled = false;
            loadedStructsNumber = loadedFromResNumber + loadedFromFiles;
        } else {
            loadedStructsNumber = loadedFromResNumber;
        }
        Logger.log("mg:loaded: " + loadedFromFiles);
        return loadedFromFiles > 0;
    }

    public static short[][] readFromDataInputStream(DataInputStream dis) throws IOException {
        if (dis == null) {
            return null;
        }

        try {
            short fileFormatVersion = dis.readShort(); // read file format version
            if (Utils.isArrContain(GameFileUtils.SUPPORTED_FILE_FORMAT_VERSIONS, fileFormatVersion)) {
                // number of primitives in the structure
                int length = 16;
                if (fileFormatVersion > 0) {
                    length = dis.readShort();
                }
                Logger.log("read: ver=" + fileFormatVersion + " length=" + length);

                short[][] structure = new short[length][];
                for (int c = 0; true; c++) {
                    short id = dis.readShort();

                    // structID 0 is end of file
                    if (id == 0) {
                        break;
                    }

                    /* read a primitive (e.g., line or circle)
                     *
                     * for example:
                     * data = {2, 0, 0, 100, 0}:
                     * id=2 (LINE), x1=0, y1=0, x2=100, y2=0
                     */
                    short[] data = new short[ARGS_NUMBER[id] + 1];
                    // first cell is ID of primitive, next cells are arguments
                    data[0] = id;
                    try {
                        for (int i = 1; i < data.length; i++) {
                            data[i] = dis.readShort();
                        }
                    } catch (EOFException ex) {
                        try {
                            dis.close();
                        } catch (Exception e) { }
                        throw ex;
                    }
                    structure[c] = data;
                }

                try {
                    dis.close();
                } catch (Exception e) { }
                return structure;
            } else {
                Logger.log("Unsupported file format version: " + fileFormatVersion);
                try {
                    dis.close();
                } catch (Exception e) { }
                return null;
            }
        } catch(ArrayIndexOutOfBoundsException ex) {
            Logger.log("error parsing file " + ex);
            ex.printStackTrace();
            try {
                dis.close();
            } catch (Exception e) { }
            return null;
        }
    }

    void saveStructToStorage(short[][] data) {
        Logger.log("savivg new structure, id=" + loadedStructsNumber);
        structStorage = increaseArrayIfNeeded(structStorage, loadedStructsNumber, 8);
        structStorage[loadedStructsNumber] = data;
        loadedStructsNumber++;
    }

    short[][][] increaseArrayIfNeeded(short[][][] array, int newElementIndex, int inc) {
        if (newElementIndex >= array.length) {
            short[][][] newArray = new short[array.length + inc][][];
            System.arraycopy(array, 0, newArray, 0, array.length);
            return newArray;
        }
        return array;
    }
}
