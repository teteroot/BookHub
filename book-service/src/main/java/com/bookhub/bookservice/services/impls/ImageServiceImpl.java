package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.config.properties.CoverProperties;
import com.bookhub.bookservice.exceptions.extensions.UnsupportedCoverTypeException;
import com.bookhub.bookservice.exceptions.extensions.UnsupportedImageAspectRatio;
import com.bookhub.bookservice.services.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageServiceImpl implements ImageService {

    private final CoverProperties coverProperties;


    @Override
    public void validateFormat(InputStream imageStream, double targetRatio, double ratioTolerance) {
        try (var in = ImageIO.createImageInputStream(imageStream)) {
            var readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new UnsupportedCoverTypeException();
            }

            var reader = readers.next();
            try {
                reader.setInput(in);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                double ratio = (double) width / height;
                if (Math.abs(targetRatio - ratio) > ratioTolerance) {
                    throw new UnsupportedImageAspectRatio(width, height);
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException | NullPointerException e) {
            throw new UnsupportedCoverTypeException();
        }
    }

    @Override
    public void convertToCommonFormat(InputStream imageStream, OutputStream outputStream) {
        try {
            Thumbnails.of(imageStream)
                    .scale(1)
                    .outputFormat(coverProperties.getCommonType().getSubtype())
                    .toOutputStream(outputStream);
        }catch (IOException|NullPointerException e) {
            throw new UnsupportedCoverTypeException();
        }
    }

    @Override
    public MediaType getCommonCoverType() {
        return coverProperties.getCommonType();
    }

    @Override
    public int getPdfDpi() {
        return coverProperties.getDpi();
    }


}
