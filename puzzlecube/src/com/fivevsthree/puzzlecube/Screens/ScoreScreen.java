package com.fivevsthree.puzzlecube.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Controllers.ScreenController;
import com.fivevsthree.puzzlecube.Models.Score;

public class ScoreScreen implements Screen {

	private PuzzleCube game;

	private Stage stage;
	private Skin skin;

	private ScreenController screenController;
	private InputMultiplexer inputMultiplexer;

	private ScrollPane scrollPane;

	private TextButton backButton, clearButton;

	private Dialog confirmationDialog;

	private enum ConfirmationResult {
		YES, NO
	};

	public ScoreScreen(PuzzleCube game) {
		this.game = game;

		// Set up the stage
		stage = new Stage(800, 480, true);

		// Get skin asset
		skin = game.getAssets().get("data/skins/ui.json");

		createUI();

		screenController = new ScreenController(game);
		inputMultiplexer = new InputMultiplexer(screenController, stage);
	}

	private void createUI() {
		backButton = new TextButton("Back to Menu", skin);
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showMainMenu();
			}
		});

		clearButton = new TextButton("Clear Scores", skin);
		clearButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				confirmationDialog.show(stage);

				for (Actor actor : stage.getActors()) {
					if (!actor.equals(confirmationDialog)) {
						actor.getColor().a = 0.25f;
					}
				}
			}
		});

		// Confirmation dialog
		confirmationDialog = new Dialog("", skin) {
			@Override
			protected void result(Object object) {
				ConfirmationResult result = (ConfirmationResult) object;

				for (Actor actor : stage.getActors()) {
					if (!actor.equals(confirmationDialog)) {
						actor.getColor().a = 1f;
					}
				}

				switch (result) {
				case YES:
					game.getBestScores().clear();
					game.showMainMenu();
					break;

				default:
					break;
				}
			}
		};

		Table buttonTable = confirmationDialog.getButtonTable();

		confirmationDialog.getCell(buttonTable).expandX().fillX();
		buttonTable.defaults().expandX().fillX();

		confirmationDialog.text("All scores will be lost.\nAre you sure?");
		confirmationDialog.button("Yes", ConfirmationResult.YES);
		confirmationDialog.button("No", ConfirmationResult.NO);
		confirmationDialog.setMovable(false);
		confirmationDialog.setTransform(false);

		scrollPane = new ScrollPane(null);
		scrollPane.setFillParent(true);
		scrollPane.setTransform(false);

		// No fading
		Dialog.fadeDuration = 0f;

		scrollPane = new ScrollPane(null);
		scrollPane.setTransform(false);
	}

	private void createScoreTable() {
		float width = stage.getWidth();

		Table scoreTable = new Table(skin);
		scoreTable.setClip(true);
		scoreTable.defaults().spaceBottom(10);

		int i = 1;
		for (Score score : game.getBestScores().getScores()) {
			scoreTable.row().expandX();
			((Label) scoreTable.add(String.valueOf(i++)).width(width * 0.1f)
					.getWidget()).setAlignment(Align.center);
			((Label) scoreTable.add(score.getDateString()).width(width * 0.4f)
					.getWidget()).setAlignment(Align.center);
			((Label) scoreTable.add(score.getTimeString()).width(width * 0.25f)
					.getWidget()).setAlignment(Align.center);
			((Label) scoreTable.add(score.getMovesString())
					.width(width * 0.25f).getWidget())
					.setAlignment(Align.center);
		}

		scrollPane.setWidget(scoreTable);
		scrollPane.setScrollingDisabled(true, false);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Update actors on the stage
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		createScoreTable();

		float width = stage.getWidth();

		Table header = new Table(skin);
		header.setTransform(false);
		header.defaults().spaceBottom(10);

		header.row().expandX();
		((Label) header.add("#").width(width * 0.1f).getWidget())
				.setAlignment(Align.center);
		((Label) header.add("Date").width(width * 0.4f).getWidget())
				.setAlignment(Align.center);
		((Label) header.add("Time").width(width * 0.25f).getWidget())
				.setAlignment(Align.center);
		((Label) header.add("Moves").width(width * 0.25f).getWidget())
				.setAlignment(Align.center);

		Table footer = new Table(skin);
		footer.setTransform(false);
		footer.defaults().spaceBottom(10);

		Image timeImage = new Image(skin, "time-image");
		timeImage.setAlign(Align.left);
		timeImage.setScaling(Scaling.none);

		Image bestImage = new Image(skin, "star-image");
		bestImage.setAlign(Align.center);
		bestImage.setScaling(Scaling.none);

		Image movesImage = new Image(skin, "moves-image");
		movesImage.setAlign(Align.right);
		movesImage.setScaling(Scaling.none);

		footer.row().expandX();
		footer.add(timeImage).left().width(width * 0.08f);
		((Label) footer
				.add(game.getBestScores().getBestSeconds().getTimeString())
				.width(width * 0.38f).getWidget()).setAlignment(Align.left);
		footer.add(bestImage).center().width(width * 0.08f);
		((Label) footer
				.add(game.getBestScores().getBestMoves().getMovesString())
				.width(width * 0.38f).getWidget()).setAlignment(Align.right);
		footer.add(movesImage).right().width(width * 0.08f);

		Table table = new Table(skin);
		table.setFillParent(true);
		table.setTransform(false);

		float cellWidth = stage.getWidth() / 2;

		table.row();
		table.add(backButton).width(cellWidth);
		table.add(clearButton).width(cellWidth);

		table.row().expandX();
		table.add(header).colspan(2).fillX();

		table.row().expand().top();
		table.add(scrollPane).colspan(2).fillX();

		table.row().expandX().bottom();
		table.add(footer).colspan(2).fillX();

		stage.clear();
		stage.addActor(table);

		// Process input from our stage
		Gdx.input.setInputProcessor(inputMultiplexer);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		if (stage != null) {
			stage.dispose();
			stage = null;
		}
	}

}
