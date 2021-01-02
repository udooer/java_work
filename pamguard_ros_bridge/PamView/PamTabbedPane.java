package PamView;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JTabbedPane;

import PamController.PamControllerInterface;
import PamView.PamColors.PamColor;

public class PamTabbedPane extends JTabbedPane implements ColorManaged {
	
	private PamGui pamGui;
	
	private PamControllerInterface pamControllerInterface;
	
	public PamTabbedPane(PamControllerInterface pamControllerInterface, PamGui pamGui) {
		super();
		this.pamControllerInterface = pamControllerInterface;
		this.pamGui = pamGui;

		this.setOpaque(true);
		
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}
	private PamColor defaultColor = PamColor.BORDER;
	
	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}

	@Override
	public Component add(String name, Component component) {
		Component c = super.add(name, component);
		
		return c;
	}
	
	@Override
	protected void processMouseEvent(MouseEvent evt)
    {
      if (evt.getID() == MouseEvent.MOUSE_PRESSED
          && evt.getButton() == MouseEvent.BUTTON3)
      {
        int index = indexAtLocation(evt.getX(), evt.getY());
        //        System.out.println("Hit tab index " + index);
        if (index >= 0) {
        	pamControllerInterface.getGuiFrameManager().getTabPopupMenu(pamGui, index).
        	show(this, evt.getX(), evt.getY());
        }
      }
      else
      {
        super.processMouseEvent(evt);
      }
    }
  
    	
}
