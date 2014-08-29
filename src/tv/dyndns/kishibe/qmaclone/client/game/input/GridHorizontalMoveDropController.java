package tv.dyndns.kishibe.qmaclone.client.game.input;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.GridConstrainedDropController;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class GridHorizontalMoveDropController extends GridConstrainedDropController {
	private int startY = Integer.MAX_VALUE;

	public GridHorizontalMoveDropController(AbsolutePanel dropTarget, int gridX, int gridY) {
		super(dropTarget, gridX, gridY);
	}

	public void setStartY(int startY) {
		this.startY = startY;
	}

	@Override
	public void onDrop(DragContext context) {
		context.desiredDraggableY = startY;
		super.onDrop(context);
		startY = Integer.MAX_VALUE;
	}

	@Override
	public void onMove(DragContext context) {
		context.desiredDraggableY = startY;
		super.onMove(context);
	}
}
