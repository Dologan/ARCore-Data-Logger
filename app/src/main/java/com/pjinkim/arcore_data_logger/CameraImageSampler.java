package com.pjinkim.arcore_data_logger;

import android.media.Image;

import java.nio.ByteBuffer;

/**
 * Reads RGB values straight out of an ARCore {@link Image} in YUV_420_888 form.
 *
 * <p>This replaces the previous path, which for every single frame built an NV21 buffer,
 * JPEG-compressed it at quality 100, decoded the JPEG back into a {@link android.graphics.Bitmap}
 * and rotated it — all to read a few dozen pixels. At 30 fps on the scene-update thread that was
 * the app's dominant frame-rate cost, and it is worst on the older devices this project still
 * needs to support.
 *
 * <p>Coordinates are expressed in the same rotated (portrait) frame the old rotated bitmap used,
 * so callers and the resulting point colours are unchanged. A 90-degree clockwise rotation maps
 * source (sx, sy) to destination (srcHeight - 1 - sy, sx), which inverts to sx = dy and
 * sy = srcHeight - 1 - dx.
 */
public class CameraImageSampler {

    private final int mSrcWidth;
    private final int mSrcHeight;

    private final ByteBuffer mYBuffer;
    private final ByteBuffer mUBuffer;
    private final ByteBuffer mVBuffer;

    private final int mYRowStride;
    private final int mYPixelStride;
    private final int mUVRowStride;
    private final int mUVPixelStride;

    public CameraImageSampler(Image image) {
        mSrcWidth = image.getWidth();
        mSrcHeight = image.getHeight();

        Image.Plane[] planes = image.getPlanes();
        // duplicate() gives each plane an independent position, so reads do not disturb the
        // buffer state ARCore may still be using.
        mYBuffer = planes[0].getBuffer().duplicate();
        mUBuffer = planes[1].getBuffer().duplicate();
        mVBuffer = planes[2].getBuffer().duplicate();

        mYRowStride = planes[0].getRowStride();
        mYPixelStride = planes[0].getPixelStride();
        mUVRowStride = planes[1].getRowStride();
        mUVPixelStride = planes[1].getPixelStride();
    }

    /** Width of the rotated (portrait) frame. */
    public int getWidth() {
        return mSrcHeight;
    }

    /** Height of the rotated (portrait) frame. */
    public int getHeight() {
        return mSrcWidth;
    }

    /**
     * Samples one pixel of the rotated frame.
     *
     * @return packed 0xRRGGBB, or -1 if the coordinate falls outside the image.
     */
    public int sampleRgb(int dx, int dy) {
        if (dx < 0 || dx >= getWidth() || dy < 0 || dy >= getHeight()) {
            return -1;
        }

        // Undo the 90-degree clockwise rotation.
        int sx = dy;
        int sy = mSrcHeight - 1 - dx;

        int y = mYBuffer.get(sy * mYRowStride + sx * mYPixelStride) & 0xFF;

        // Chroma planes are subsampled 2x2.
        int uvIndex = (sy / 2) * mUVRowStride + (sx / 2) * mUVPixelStride;
        int u = mUBuffer.get(uvIndex) & 0xFF;
        int v = mVBuffer.get(uvIndex) & 0xFF;

        // BT.601 full-range, matching what the old YuvImage/JPEG round trip produced.
        int cu = u - 128;
        int cv = v - 128;
        int r = clamp((int) (y + 1.402f * cv));
        int g = clamp((int) (y - 0.344136f * cu - 0.714136f * cv));
        int b = clamp((int) (y + 1.772f * cu));

        return (r << 16) | (g << 8) | b;
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 255);
    }
}
