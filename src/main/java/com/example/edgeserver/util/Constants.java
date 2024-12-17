package com.example.edgeserver.util;

import java.io.File;

public class Constants {
    public static final String IMAGE_BASE_DIRECTORY = new File(System.getProperty("user.dir")).getParent() + File.separator + "image_base";
    public static final String IMAGE_COMBINED_DIRECTORY = new File(System.getProperty("user.dir")).getParent() + File.separator + "image_combined";

    public static final String IMAGE_STICKMAN_DIRECTORY = new File(System.getProperty("user.dir")).getParent() + File.separator + "image_stickMan";

    public static final String IMAGE_PROCESSED_DIRECTORY = new File(System.getProperty("user.dir")).getParent() + File.separator + "image_processed";
}
