Edit the following file
runelite-client\src\main\java\net\runelite\client\ui\overlay\OverlayUtil.java

Add or modify the functions below to make it work...

	public static void drawStrokeAndFillPoly(Graphics2D graphics, Color color, int strokeWidth,
											 int outlineAlpha, int fillAlpha, Polygon poly) {
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), outlineAlpha));
		graphics.setStroke(new BasicStroke(strokeWidth));
		graphics.draw(poly);
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha));
		graphics.fill(poly);
	}

	public static void drawTiles(Graphics2D graphics, Client client, WorldPoint point,
								 WorldPoint playerPoint, Color color, int strokeWidth, int outlineAlpha, int fillAlpha) {
		if (point.distanceTo(playerPoint) >= 32) {
			return;
		}
		LocalPoint lp = LocalPoint.fromWorld(client, point);
		if (lp == null) {
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly == null) {
			return;
		}
		drawStrokeAndFillPoly(graphics, color, strokeWidth, outlineAlpha, fillAlpha, poly);
	}