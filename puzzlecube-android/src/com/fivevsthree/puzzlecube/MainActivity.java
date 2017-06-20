package com.fivevsthree.puzzlecube;

import android.os.Bundle;
import android.text.format.DateFormat;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useWakelock = true;
		cfg.useGL20 = true;

		PuzzleCube.setDateFormat(DateFormat
				.getDateFormat(getApplicationContext()));

		initialize(new PuzzleCube(), cfg);
	}
}