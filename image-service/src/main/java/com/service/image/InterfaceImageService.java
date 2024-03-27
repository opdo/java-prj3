package com.service.image;

import java.awt.image.BufferedImage;

public interface InterfaceImageService {
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold);
}
