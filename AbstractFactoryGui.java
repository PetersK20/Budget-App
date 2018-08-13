import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;


//Using the Abstract Factory Design Pattern to create the Gui
public class AbstractFactoryGui {
	// This returns the Frame to the User
	public ProjectFrames orderFrame(String identifior, FrameFacade individualFacade) {
		ProjectFrames returnFrame = getFrame(identifior, individualFacade);
		return returnFrame;
	}

	// Creates the factory and Frame object based on the string passed
	private ProjectFrames getFrame(String identifior, FrameFacade individualFacade) {
		ProjectFrames frameClass = null;
		Factory factory=null;
		// Checks what type of frame the user wants and sends a factory object to a frameClass
		if (identifior.equals("Budget")) {
			factory = new BudgetFactory(individualFacade);
		} else if (identifior.equals("Reconciliation")) {
			factory = new ReconciliationFactory(individualFacade);
		}else if (identifior.equals("Charts")) {
			factory = new ChartsFactory(individualFacade);
		}
		frameClass = new ProjectFrames(factory);
		return frameClass;
	}
}// End of AbstractFactoryGui
	// Use Singleton because the 4 frames have the same FrameChooser (Top Panel)

abstract class Factory {
	protected FrameFacade individualFac;

	public abstract JComponent createCenterPanel();
	// returns the panel used to navigate through the 4 Frames
	public JPanel createFrameChooser() {
		return FrameChooser.getInstance().makePanel();
	}

	// returns the topPanel
	public JPanel createTopPanel() {
		return TopPanel.getInstance(individualFac).makePanel();
	}
}// End of Factory

class BudgetFactory extends Factory {
	// Sets the facade class Variable
	public BudgetFactory(FrameFacade individualFac) {
		this.individualFac = individualFac;
	}

	// returns Budget's centerPanel to the Budget Frame
	public JScrollPane createCenterPanel() {
		return new BudgetCenter().makeCenterPanel(individualFac);
	}
}// End of BudgetFactory

class ReconciliationFactory extends Factory {

	// Sets the facade class Variable
	public ReconciliationFactory(FrameFacade individualFac) {
		this.individualFac = individualFac;
	}

	// returns Reconciliation's centerPanel to the Budget Frame
	public JScrollPane createCenterPanel() {
		return new ReconciliationCenter().makeCenterPanel(individualFac);
	}
}// End of ReconciliationFactory

class ChartsFactory extends Factory {

	// Sets the facade class Variable
	public ChartsFactory(FrameFacade individualFac) {
		this.individualFac = individualFac;
	}

	public JScrollPane createCenterPanel() {
		return new ChartsCenter().makeCenterPanel(individualFac);
	}

}// End of ChartsFactory




class FrameChooser {
	private JButton budgetButton, chartButton, recButton;
	private JPanel returnPanel;
	private Font buttonFont=new Font("Serif",Font.PLAIN,15);
	private Color blueColor = new Color(51, 153, 255);
	private static FrameChooser singleInstance = null;
	private ButtonListener buttonListener = new ButtonListener();
	private boolean budgetSelected=true,recSelected=false,chartSelected=false;
	private FrameChooser() {}

	// Only returns one instance using singleton because the panel is the same for all 4 Frames
	public static FrameChooser getInstance() {
		if (singleInstance == null) {
			singleInstance = new FrameChooser();
		}
		return singleInstance;
	}

	// Calls other methods and returns the returnPanel
	public JPanel makePanel() {
		if (returnPanel == null) {
			returnPanel = new JPanel();
			createComponents();
		}
		return returnPanel;
	}

	// Creates the components
	private void createComponents() {
		budgetButton = FactoryFacade.makeTextButtons("Budget", blueColor, Color.WHITE,buttonFont);
		addListeners(budgetButton);
		recButton = FactoryFacade.makeTextButtons("Reconciliation", Color.WHITE, blueColor,buttonFont);
		addListeners(recButton);
		chartButton = FactoryFacade.makeTextButtons("Charts", Color.WHITE, blueColor,buttonFont);
		addListeners(chartButton);
		createPanel(budgetButton, chartButton, recButton);
	}

	// Adds the components to the JPanel
	private void createPanel(JButton budgetButton, JButton chartsButton, JButton recButton) {
		returnPanel.setLayout(new GridLayout(1, 4));
		returnPanel.setBackground(Color.WHITE);
		returnPanel.add(budgetButton);
		returnPanel.add(recButton);
		returnPanel.add(chartsButton);
	}

	// Adds the Mouse and Action Listener to the buttons
	private void addListeners(JButton button) {
		button.addActionListener(buttonListener);
		button.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				button.setForeground(Color.WHITE);
				button.setBackground(blueColor);
			}

			public void mouseExited(MouseEvent e) {
				if(!checkIfSelected()){
					button.setForeground(blueColor);
					button.setBackground(Color.WHITE);
				}
			}
			//Checks which button is selected
			private Boolean checkIfSelected(){
				if(button.getText().equals("Budget")){
					return budgetSelected;
				}else if(button.getText().equals("Reconciliation")){
					return recSelected;
				}else{
					return chartSelected;
				}
			}
		});// End of MouseListener
	}

	class ButtonListener implements ActionListener {

		// Checks which button threw the action
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == budgetButton && !budgetSelected) {
				actOnBudget();
			} else if (e.getSource() == recButton && !recSelected) {
				actOnRec();
			}else if(e.getSource()==chartButton && !chartSelected){
				actOnCharts();
			}
		}
		
		//Method called when BudgetButton is pressed
		private void actOnBudget() {
			setSelected(true,false,false);
			resetButtonColor();
			budgetButton.setForeground(Color.WHITE);
			budgetButton.setBackground(blueColor);
			startUp.createBudgetFrame();
		}
		//Method called when RecButton is pressed
		private void actOnRec() {
			setSelected(false,true,false);
			resetButtonColor();
			recButton.setForeground(Color.WHITE);
			recButton.setBackground(blueColor);
			startUp.createRecFrame();
		}
		//Method called when ChartButton is pressed
		private void actOnCharts() {
			setSelected(false,false,true);
			resetButtonColor();
			chartButton.setForeground(Color.WHITE);
			chartButton.setBackground(blueColor);
			startUp.createChartFrame();
		}
		
		
		//sets the new Selected button and unselects the previously selected button
		private void setSelected(boolean budgetSelect,boolean recSelect,boolean chartSelect){
			budgetSelected=budgetSelect;
			recSelected=recSelect;
			chartSelected=chartSelect;
		}
		//Resets the ButtonColor so the previously selected button will not stay as a selected Color 
		private void resetButtonColor(){
			budgetButton.setForeground(blueColor);
			budgetButton.setBackground(Color.WHITE);
			recButton.setForeground(blueColor);
			recButton.setBackground(Color.WHITE);
			chartButton.setForeground(blueColor);
			chartButton.setBackground(Color.WHITE);
		}
	}// End of ButtonListener
}// End of FrameChooser