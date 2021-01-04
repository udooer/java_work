package PamView;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import PamView.PamColors.PamColor;

public class PamPanel extends JPanel implements ColorManaged{

	public PamPanel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PamPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public PamPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public PamPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public PamPanel(PamColor defaultColor) {
		super();
		setDefaultColor(defaultColor);
	}

	private PamColor defaultColor = PamColor.BORDER;
	
	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		PamColors.getInstance().setColor(this, defaultColor);
		this.defaultColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}
}
