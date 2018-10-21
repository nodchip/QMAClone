package tv.dyndns.kishibe.qmaclone.client.report;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;

public interface CellTableProblemResources extends Resources {
	@Source("CellTableProblem.css")
	Style cellTableStyle();

	public static class Factory {
		private static CellTableProblemResources resources;

		public static CellTableProblemResources get() {
			if (resources == null) {
				resources = GWT.create(CellTableProblemResources.class);
			}
			return resources;
		}
	}
}
