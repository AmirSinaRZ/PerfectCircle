package com.amirsinarz.perfectcircle;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;


public class CircleDrawingGame extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private Vector2 centerPoint;
    private List<Vector2> pathPoints;
    private boolean isDrawing;
    private Float initialRadius;
    private float totalError;
    private int pointsCount;
    private boolean showScore;
    private boolean showStartMessage;
    private float accuracyScore;

    @Override
    public void create() {
        OrthographicCamera camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        camera.update();

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);
        font.setColor(Color.WHITE);

        centerPoint = new Vector2(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        pathPoints = new ArrayList<>();
        resetGame();
    }

    private void resetGame() {
        isDrawing = false;
        initialRadius = null;
        totalError = 0;
        pointsCount = 0;
        showScore = false;
        showStartMessage = true;
        accuracyScore = 0;
        pathPoints.clear();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //center of circle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(centerPoint.x, centerPoint.y, 5);
        shapeRenderer.end();


        if (showStartMessage && initialRadius == null) {
            spriteBatch.begin();
            String startText = "Touch to start";
            float textX = centerPoint.x - font.getRegion().getRegionWidth() / 2f;
            float textY = centerPoint.y + 30;
            font.draw(spriteBatch, startText, textX, textY);
            spriteBatch.end();
        }

        //start drawing
        if (Gdx.input.isTouched()) {
            Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

            if (!isDrawing && !showScore) {
                // calculating radius
                isDrawing = true;
                pathPoints.clear();
                totalError = 0;
                pointsCount = 0;
                showScore = false;
                showStartMessage = false;

                if (initialRadius == null) {
                    initialRadius = touchPos.dst(centerPoint);
                }
            }

            pathPoints.add(touchPos);

            // .... :/
            float distance = touchPos.dst(centerPoint);
            totalError += Math.abs(distance - initialRadius);
            pointsCount++;
        } else if (isDrawing) {
            //end of drawing circle
            isDrawing = false;
            showScore = true;

            //calc score
            if (pointsCount > 0) {
                float averageError = totalError / pointsCount * 2f;
                accuracyScore = Math.max(0, 100 - (averageError * 100 / initialRadius));
            }
        }

        //drawing circle with colors
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        if (initialRadius != null) {
            for (int i = 0; i < pathPoints.size() - 1; i++) {
                Vector2 pointA = pathPoints.get(i);
                Vector2 pointB = pathPoints.get(i + 1);

                float distanceA = pointA.dst(centerPoint);
                float colorRatio = Math.abs(distanceA - initialRadius) / initialRadius;

                Color penColor = new Color();
                if (colorRatio < 0.1f) {
                    penColor.set(Color.GREEN).lerp(Color.YELLOW, colorRatio * 10);
                } else {
                    penColor.set(Color.YELLOW).lerp(Color.RED, (colorRatio - 0.1f) * 10);
                }

                shapeRenderer.setColor(penColor);
                //shapeRenderer.line(pointA.x, pointA.y, pointB.x, pointB.y);
                //drawing multi lines
                float penWidth = 5f;
                for (float offset = -penWidth / 2; offset <= penWidth / 2; offset += 1f) {
                    shapeRenderer.line(pointA.x + offset, pointA.y + offset,
                        pointB.x + offset, pointB.y + offset);
                }

            }
        }

        shapeRenderer.end();

        //showing score
        if (showScore && initialRadius != null) {
            spriteBatch.begin();
//            String accuracyText = "Percentage: " + String.format("%.2f", accuracyScore) + "%";
            String accuracyText = "Percentage: " + Math.round(accuracyScore * 100.0) / 100.0 + "%";
            float textX = centerPoint.x - font.getRegion().getRegionWidth() / 2f;
            float textY = centerPoint.y + initialRadius + 20;
            font.draw(spriteBatch, accuracyText, textX, textY);
            spriteBatch.end();

            //restarting game
            if (Gdx.input.justTouched()) {
                resetGame();
            }
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}

