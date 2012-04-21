package eu.nazgee.prank.solar;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import eu.nazgee.game.utils.helpers.Positioner;

public class Cell extends Rectangle {

	private final Sprite mCellSprite;

	public Cell(float pX, float pY, float pWidth, float pHeight,
			Sprite pCellSprite,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pWidth, pHeight, pVertexBufferObjectManager);
		mCellSprite = pCellSprite;

		attachChild(getCellSprite());
		Positioner.setCentered(getCellSprite(), getWidth()/2, getHeight()/2);
	}

	public Sprite getCellSprite() {
		return mCellSprite;
	}
}
