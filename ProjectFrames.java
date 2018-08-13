import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.*;

public class ProjectFrames {
	protected JFrame frame = new JFrame();
	protected Factory factory=null;
	protected JPanel topPanel;
	protected JPanel frameChooserPanel;
	private JPanel northPanel=new JPanel();
	protected JScrollPane centerPanel;
	// Allows Frames to use the FactoryFacade Class
	public ProjectFrames(Factory factory) {
		this.factory=factory;
		getComponent();
		addTopComponent();
	}
	public void createFrame(Factory factory,JFrame frame){
		this.factory=factory;
		frame.remove(centerPanel);
		
		getComponent();
		addTopComponent();
		
		centerPanel.revalidate();
		frame.revalidate();

	}
	protected void getComponent(){
		topPanel=factory.createTopPanel();
		centerPanel=(JScrollPane) factory.createCenterPanel();
		frameChooserPanel=factory.createFrameChooser();
		frame.add(centerPanel,BorderLayout.CENTER);	
		frame.revalidate();
	}
	//Adds the top panel components created by getCompoenent()
	private void addTopComponent(){
		northPanel.setLayout(new GridLayout(2,1));
		northPanel.add(topPanel);
		northPanel.add(frameChooserPanel);
	
		frame.add(northPanel,BorderLayout.NORTH);
		frame.revalidate();
	}	
	//Allows the Frame to return to Main
	public final JFrame getFrame() {
		return frame;
	}
}
/*
class Budget extends ProjectFrames {
	// Passes facade object to super which allows Frames to use the
	// FactoryFacade Class
	public Budget(Factory factory) {
		super(factory);
	
	}

}

class Reconciliation extends ProjectFrames {
	// Passes facade object to super which allows Frames to use the
	// FactoryFacade Class
	public Reconciliation(Factory factory) {
		super(factory);
	}
}
class Charts extends ProjectFrames {
	// Passes facade object to super which allows Frames to use the
	// FactoryFacade Class
	public Charts(Factory factory) {
		super(factory);
	}

}*/
