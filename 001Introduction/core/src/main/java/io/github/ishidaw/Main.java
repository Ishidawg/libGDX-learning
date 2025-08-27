package io.github.ishidaw;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {

    // Texture does not store any position state. Sprite does.
    Texture background;
    Texture player;
    Array<Texture> books;
    Texture cBook;
    Texture ccBook;
    Texture phpBook;
    Texture openglBook;
    Texture jsBook;
    Sound collectSfx;
    Music soundtrack;

    SpriteBatch spriteBatch;
    FillViewport viewport;

    // Sprite
    Sprite playerSprite;
    Array<Sprite> booksSprites;

    float spawnTimer;

    // Create rectangle to be collision
    Rectangle playerRect;
    Rectangle bookRect;

    @Override
    public void create() { // executes immediately when the game is run
        // Create every texture is no a good practice, instead we need to use TexturePacker
        // this is because it loads into vram, is more practical to have a large texture file and search inside of them by the correct coordinates.
        background = new Texture("background.png");
        player = new Texture("player.png");
        cBook = new Texture("c_book.png");
        ccBook = new Texture("cc_book.png");
        phpBook = new Texture("php_book.png");
        openglBook = new Texture("opengl_book.png");
        jsBook = new Texture("js_book.png");

        books = new Array<>();
        books.addAll(cBook, ccBook, phpBook, openglBook, jsBook);

        collectSfx = Gdx.audio.newSound(Gdx.files.internal("collect.mp3"));
        soundtrack = Gdx.audio.newMusic(Gdx.files.internal("soundtrack.mp3"));

        // Sprite
        playerSprite = new Sprite(player); // player is the texture
        playerSprite.setSize(16, 16); // set the player with width/height of 16 meter. Player is 160x160
        booksSprites = new Array<>();

        // Collision
        playerRect = new Rectangle();
        bookRect = new Rectangle();

        // Soundtrack
        soundtrack.setLooping(true);
        soundtrack.setVolume(.1f);
        soundtrack.play();

        spriteBatch = new SpriteBatch();
        // We can think of a viewport as: the game at it's perfect conditions
        viewport = new FillViewport(128f, 72f); // 1280 x 720 -> 128f, 72f
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your application here. The parameters represent the new window size.
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        // Draw your application here.
        input();
        logic();
        draw();
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.
    }

    // it is more efficient to send all your draw calls at once to the graphics processing unit (GPU)
    // The process of drawing an individual texture is called a draw call.
    // The SpriteBatch is how libGDX combines these draw calls together.
    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined); // shows how the Viewport is applied to the SpriteBatch. This is necessary for the images to be shown in the correct place.
        spriteBatch.begin();
        // Our game world is described in imaginary units best defined as meters. 128 and 72.
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(background, 0, 0, worldWidth, worldHeight);
        playerSprite.draw(spriteBatch);

        for (Sprite book : booksSprites) {
            book.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void input() {
        float speed = 80f;
        float delta = Gdx.graphics.getDeltaTime(); // Delta is a measure time between frames, we use it to depend on frames per second.


        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) playerSprite.translateX(speed * delta);
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) playerSprite.translateX(-speed * delta);
    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // store the player asset size - > 160x160
        float playerWidth = playerSprite.getWidth();
        float playerHeight = playerSprite.getHeight();

        float controllerYSpeed = 20f;
        float delta = Gdx.graphics.getDeltaTime();

        playerSprite.setX(MathUtils.clamp(playerSprite.getX(), 0, worldWidth - playerWidth)); // We are subtracting the far width with the player width to get it bound inside the screen

        // Apply the collision (Rect) to the player
        playerRect.set(playerSprite.getX(), playerSprite.getY(), playerWidth, playerHeight);

        for (Sprite book : booksSprites) {

            float controllerWidth = book.getWidth();
            float controllerHeight = book.getHeight();

            book.translateY(-controllerYSpeed * delta);
            // Apply the collision
            bookRect.set(book.getX(), book.getY(), controllerWidth, controllerHeight);

            if (book.getY() < -controllerHeight) booksSprites.removeValue(book, true);
            if (playerRect.overlaps(bookRect)) {
                booksSprites.removeValue(book, true);
                collectSfx.play(.2f);
            }
        }

        spawnTimer += delta;
        if (spawnTimer > 1f) { // Check if it has been more than a second
            spawnTimer = 0; // Reset the timer
            createBook();
        }
    }

    private void createBook() {
        float controllerWidth = 13.2f;
        float controllerHeight = 13.2f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite bookSprite = new Sprite(books.random()); // controller is the texture
        bookSprite.setSize(controllerWidth, controllerHeight);
        bookSprite.setX(MathUtils.random(0f, worldWidth - controllerWidth));
        bookSprite.setY(worldHeight);
        booksSprites.add(bookSprite);
    }
}
