package br.com.econdominio.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

import static android.graphics.Shader.TileMode.CLAMP;


public class RoundRectTransformation implements Transformation {

    private final int cornerRadius;

    public RoundRectTransformation(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, CLAMP, CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "roundRect";
    }
}