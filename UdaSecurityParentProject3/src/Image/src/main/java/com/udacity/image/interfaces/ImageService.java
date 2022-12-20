package com.udacity.image.interfaces;


import java.awt.image.BufferedImage;

/**
 * Service that identifies images of cats.
 */
public interface ImageService {

    boolean imageContainsCat(BufferedImage currentCameraImage, float v);

}
