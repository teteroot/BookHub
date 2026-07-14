package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.exceptions.extensions.UnsupportedCoverTypeException;
import com.bookhub.bookservice.exceptions.extensions.UnsupportedImageAspectRatio;
import com.bookhub.bookservice.services.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {


    @Override
    public void validateFormat(InputStream imageStream, double targetRatio, double ratioTolerance) {
        try {
            var bufferedImage = ImageIO.read(imageStream);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            double ratio = (double) width / height;
            if (Math.abs(targetRatio - ratio) > ratioTolerance) {
                throw new UnsupportedImageAspectRatio(width,height);
            }
        } catch (IOException|NullPointerException e) {
            throw new UnsupportedCoverTypeException();
        }
    }


}
