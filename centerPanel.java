import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.*;

public abstract class centerPanel {
	protected ResultSet resultSetFacade;
	protected int yPos=1;
	protected JPanel returnPanel = new JPanel();
	protected Font smallFont=new Font("Serif",Font.BOLD,20);
	protected Font largeFont=new Font("Serif",Font.BOLD,25);
	protected GridBagConstraints constraintsToManipulate=new GridBagConstraints();
	// Gets the connection from the FactoryFacade and passes the info to createInstance()
	protected abstract void getConInfo();


	// Uses the connection info and ArrayList to create the panels inside the centerPanel
	
	//adds a scrollPane to the returnPanel
	protected JScrollPane addScrollPane() {
		JScrollPane pane=new JScrollPane(returnPanel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return pane;
	}
}// End of centerPanel interface





//TODO     START OF BUDGET
class BudgetCenter extends centerPanel {
	private ArrayList<BudgetInstance> save = new ArrayList<BudgetInstance>();
	private ArrayList<BudgetInstance> variable = new ArrayList<BudgetInstance>();
	private ArrayList<BudgetInstance> fixed = new ArrayList<BudgetInstance>();
	private BudgetFacade individualFac;
	private ResultSet personelDataSet;
	private Color bluePanelColor=new Color(153,204,255);
	private Color redPanelColor=new Color(255,153,153);
	private Color greenPanelColor=new Color(153,255,153);
	private double savingSpent,savingTotal,fixedSpent,fixedTotal,variableSpent,variableTotal;
	private DecimalFormat decimalFormat=new DecimalFormat("###,###.##");
	private String income,expenses;
	private BudgetInstance selectedPanel=null;
	private JPopupMenu popupMenu;
	private JMenuItem withdraw,setTitle,setAllocatedMoney,remove;
	private ButtonListener buttonListener=new ButtonListener();
	private int dataBaseIndex=1;
	private String projectedExpenses;
	
	private JLabel ratio;
	// calls other methods that will manipulate returnPanel
	public JScrollPane makeCenterPanel(FrameFacade individualFac) {
		this.individualFac = (BudgetFacade) individualFac;
		createPopUp();
		getConInfo();
		createSmallerComponent();
		yPos=1;
		return addScrollPane();
	}

	// Creates a popUpMenu that will show whenever the editButton is pressed on one of the panels
	protected void createPopUp() {
		popupMenu=new JPopupMenu();
		withdraw=createMenuItems("withdraw",popupMenu);
		setTitle=createMenuItems("Set Title",popupMenu);
		setAllocatedMoney=createMenuItems("Set Allocated Money    ",popupMenu);
		remove=createMenuItems("remove",popupMenu);
	}
	private JMenuItem createMenuItems(String label,JPopupMenu popupMenu){
		JMenuItem menuItem =new JMenuItem(label);
		popupMenu.add(menuItem);
		menuItem.addActionListener(buttonListener);
		return menuItem;
	}
	// Gets the connection from the FactoryFac and passes the info to createInstance()
	protected void getConInfo() {
		personelDataSet=individualFac.getConnectionInfo("SELECT * From personel_data");
		resultSetFacade=individualFac.getConnectionInfo("SELECT * From envelope");
		setConInfo();
		getDefaultMoneyValues();
		addDefaultInstance();
	}
	//Sets the data from the resultSet to the class variables
	private void setConInfo(){
		try {
			while(resultSetFacade.next()){
				//double moneySpent, double totalMoney, String expenseType, String label,int dataBaseIndex
				BudgetInstance instance=new BudgetInstance(resultSetFacade.getDouble(3),resultSetFacade.getDouble(4),resultSetFacade.getString(5),resultSetFacade.getString(2),dataBaseIndex);
				addEnvelopeInstance(instance);
				dataBaseIndex++;
			}
			personelDataSet.next();
			income=decimalFormat.format(personelDataSet.getDouble(4));
			expenses=decimalFormat.format(personelDataSet.getDouble(5));
			projectedExpenses=decimalFormat.format(personelDataSet.getDouble(7));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//Gets the moneyRatios for the saving,fixed,and variable
	protected void getDefaultMoneyValues(){
		for(BudgetInstance instance:save){
			savingSpent+=instance.getMoneySpent();
			savingTotal+=instance.getTotalMoney();
		}
		for(BudgetInstance instance:variable){
			variableSpent+=instance.getMoneySpent();
			variableTotal+=instance.getTotalMoney();
		}
		for(BudgetInstance instance:fixed){
			fixedSpent+=instance.getMoneySpent();
			fixedTotal+=instance.getTotalMoney();
		}
	}
	//Adds instances of the BudgetTemplateTabs
	protected void addDefaultInstance(){
		save.add(0,new BudgetInstance(savingSpent,savingTotal,"Save","Savings : ",0));
		fixed.add(0,new BudgetInstance(fixedSpent,fixedTotal,"Fixed","Fixed :  ",0));
		variable.add(0,new BudgetInstance(variableSpent,variableTotal,"Variable","Variable:",0));
	}
	//Adds instances of the Envelopes
	protected void addEnvelopeInstance(BudgetInstance instance) {
		String type=instance.getExpenseType();
		if(type.equals("Save")){
			save.add(instance);
		}else if(type.equals("Variable")){
			variable.add(instance);
		}else if(type.equals("Fixed")){
			fixed.add(instance);
		}
	}
	// Uses the connection info and ArrayList to create the panels inside the centerPanel
	protected void createSmallerComponent() {
		returnPanel.setLayout(new GridBagLayout());
		createIncomeComponents(new Color(102, 178, 255));
		makeDefaults(fixed.get(0),bluePanelColor);
		for (int x = 1; x < fixed.size(); x++) {
			makeEnvelopes(fixed.get(x),bluePanelColor);
		}
		makeDefaults(variable.get(0),redPanelColor);
		for (int x = 1; x < variable.size(); x++) {
			makeEnvelopes(variable.get(x),redPanelColor);
		}
		makeDefaults(save.get(0),greenPanelColor);
		for (int x = 1; x < save.size(); x++) {
			makeEnvelopes(save.get(x),greenPanelColor);
		}
	}

	// Creates the envelopes that are a part of the Fixed type
	protected void makeEnvelopes(BudgetInstance BInstance,Color color) {
		//initialize Envelope Panels
		JPanel envelope=new JPanel();
		envelope.setLayout(new GridBagLayout());
		envelope.setBackground(color);
		//Creates components
		JButton edit=FactoryFacade.makeIconButtons("C:/Users/Kyle Peters/Documents/picturesForFinance/editButton.png", color,50,50);
		BInstance.setButton(edit);
		edit.addActionListener(buttonListener);
		JLabel title=FactoryFacade.makeLabels(BInstance.getLabel(), smallFont);
		JLabel moneySpent=FactoryFacade.makeLabels("Money Spent : "+Double.toString(BInstance.getMoneySpent()), smallFont);
		
		JSlider moneySlider=FactoryFacade.makeSliders(0,(int)BInstance.getTotalMoney(),(int)BInstance.getMoneySpent(),color);
		JLabel totalMoney=FactoryFacade.makeLabels(Double.toString(BInstance.getTotalMoney()), smallFont);
		JLabel MinimumLabel=FactoryFacade.makeLabels(Integer.toString(0), smallFont);
		
		setEnvelopesLayout(constraintsToManipulate,envelope,edit,title,moneySpent,totalMoney,MinimumLabel,moneySlider);
	}
	//Adds the components of makeEnvelope to returnPanel
	protected void setEnvelopesLayout(GridBagConstraints c,JPanel panel,JButton edit,JLabel title,JLabel moneySpent,JLabel totalMoney,JLabel MinimumLabel,JSlider moneySlider){
		FactoryFacade.addWithGridBag(1, 1, 1, 3, 50, 50, GridBagConstraints.BOTH, GridBagConstraints.EAST, c, panel, edit);
		FactoryFacade.addWithGridBag(2, 1, 3, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, title);
		FactoryFacade.addWithGridBag(2, 2, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.EAST, c, panel, MinimumLabel);
		FactoryFacade.addWithGridBag(3, 2, 1, 1, 50, 50, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, panel, moneySlider);
		FactoryFacade.addWithGridBag(4, 2, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.WEST, c, panel, totalMoney);
		FactoryFacade.addWithGridBag(2, 3, 3, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, moneySpent);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));

		FactoryFacade.addWithGridBag(1,yPos,1,1,50,50, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, returnPanel, panel);
		yPos++;
		
	}
	// Creates the default envelope for Fixed
	protected void makeDefaults(BudgetInstance BInstance,Color color) {
		JPanel defaultPanels=new JPanel();
		defaultPanels.setLayout(new GridBagLayout());
		defaultPanels.setBackground(color);
		JButton add=FactoryFacade.makeIconButtons("C:/Users/Kyle Peters/Documents/picturesForFinance/addButton.png", color,50,50);
		BInstance.setButton(add);
		add.addActionListener(buttonListener);
		JLabel title=FactoryFacade.makeLabels(BInstance.getLabel(), largeFont);
		JLabel moneyRatio=FactoryFacade.makeLabels("$"+BInstance.getMoneySpent()+" / $"+BInstance.getTotalMoney(), largeFont);
		setDefaultsLayout(constraintsToManipulate,defaultPanels,add,title,moneyRatio);
	}
	protected void setDefaultsLayout(GridBagConstraints c,JPanel defaultPanels,JButton add,JLabel title,JLabel moneyRatio){
		//initialize defaultPanel
		FactoryFacade.addWithGridBag(1, 1, 1, 1, 50, 50, GridBagConstraints.BOTH, GridBagConstraints.WEST, c, defaultPanels, add);
		FactoryFacade.addWithGridBag(2, 1, 1, 1, 100, 100, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, defaultPanels, title);
		FactoryFacade.addWithGridBag(3, 1, 1, 1, 100, 100, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, defaultPanels, moneyRatio);
		defaultPanels.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
		//create Components
		FactoryFacade.addWithGridBag(1, yPos, 1, 1, 50, 50, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, returnPanel, defaultPanels);
		yPos++;
	}
	//Creates the IncomePanelComponents
	private void createIncomeComponents(Color color){
		JPanel panel=new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(color);
		JButton setIncome=FactoryFacade.makeTextButtons("Set Income", new Color(51, 153, 255), Color.BLACK,largeFont);
		setIncome.setBorderPainted(true);
		ratio=FactoryFacade.makeLabels("Income: $"+income+" | "+"Expenses: $"+expenses+" |Projected Expenses : $"+projectedExpenses,largeFont);
		createIncomePanel(constraintsToManipulate,panel,setIncome,ratio);
		addListenerToIncome(setIncome,ratio);
	}
	//Adds a listener to the setIncomeButton
	private void addListenerToIncome(JButton setIncome,JLabel incomeLabel){
		setIncome.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				String newIncome=JOptionPane.showInputDialog(null,"Type in your new Income");
				if(newIncome==null)return;
				newIncome=newIncome.replaceAll(",", "");
				updateLabels(newIncome);
				updateDataBase(newIncome);
				if(individualFac.checkIncomeVsExpense(personelDataSet)){
					JOptionPane.showMessageDialog(null, "Your expenses are more than your income, try balancing your budget,");
				}
			}
			
			//Sets the text on the labels
			private void updateLabels(String newIncome){
				income=decimalFormat.format(Double.parseDouble(newIncome));
				incomeLabel.setText("Income: $"+income+" | "+"Expenses: $"+expenses+" |Projected Expenses : $"+projectedExpenses);
			}
			
			//updates the dataBase
			private void updateDataBase(String newIncome){
				try {
					personelDataSet.absolute(1);
					personelDataSet.updateDouble(4, Double.parseDouble(newIncome));
					personelDataSet.updateRow();
				} catch (SQLException e1) {}	
			}
		});
	}
	//Creates the IncomePanel
	private void createIncomePanel(GridBagConstraints c,JPanel panel,JButton setIncome, JLabel ratio){
		FactoryFacade.addWithGridBag(1, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.EAST, c, panel, setIncome);
		c.insets=new Insets(0,50,0,0);
		FactoryFacade.addWithGridBag(2, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.WEST, c, panel, ratio);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
		//create Components
		c.insets=new Insets(0,0,0,0);
		FactoryFacade.addWithGridBag(1, yPos, 1, 1, 50, 50, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, returnPanel, panel);
		yPos++;
	}
	//Listens to the edit buttons on the envelopes and add buttons on the defaults
	//TODO Budget ActionListener
	class ButtonListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			//If source is a default have checkDefaults return true so checkEnvelopes is not called
				if(checkDefaults(e)){			
				}else{
					checkEnvelopes(e);
				}
				checkPopUp(e);
		}
		//Checks if the default panels threw the action
		private boolean checkDefaults(ActionEvent e){
			if(e.getSource()==fixed.get(0).getButton()){
				insertFixed();
				createDefaultFrame();
				return true;
			}else if(e.getSource()==variable.get(0).getButton()){
				insertVariable();
				createDefaultFrame();
				return true;
			}else if(e.getSource()==save.get(0).getButton()){
				insertSave();
				createDefaultFrame();
				return true;
			}
			return false;
		}
		//Inserts specific values for Fixed
		private void insertFixed(){
			try {
				resultSetFacade.moveToInsertRow();
				resultSetFacade.updateString(5,"Fixed");
				resultSetFacade.updateDouble(3,0);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//Inserts specific values for Fixed
		private void insertVariable(){
			try {
				resultSetFacade.moveToInsertRow();
				resultSetFacade.updateString(5,"Variable");
				resultSetFacade.updateDouble(3,0);
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}
		//Inserts specific values for Save
		private void insertSave(){
			try {
				resultSetFacade.moveToInsertRow();
				resultSetFacade.updateString(5,"Save");
				resultSetFacade.updateDouble(3,0);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//Checks if the envelope panels threw the action
		private void checkEnvelopes(ActionEvent e){
			for (int x = 1; x < fixed.size(); x++) {
				if(e.getSource()==fixed.get(x).getButton()){
					popupMenu.show(fixed.get(x).getButton(), 70, 0);
					selectedPanel=fixed.get(x);
					return;
				}
			}
			for (int x = 1; x < variable.size(); x++) {
				if(e.getSource()==variable.get(x).getButton()){
					popupMenu.show(variable.get(x).getButton(), 70, 0);
					selectedPanel=variable.get(x);
					return;
				}
			}
			for (int x = 1; x < save.size(); x++) {
				if(e.getSource()==save.get(x).getButton()){
					popupMenu.show(save.get(x).getButton(), 70, 0);
					selectedPanel=save.get(x);
					return;
				}
			}		
		}
		//Creates the Frame for the defaults
		private void createDefaultFrame(){		
			JFrame frame=new JFrame();
			frame.setAlwaysOnTop(true);
			frame.setResizable(false);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(400, 300);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			frame.setTitle("Add Envelopse");
			addWindowListener(frame);
			frame.add(createDefaultComponents(frame));
		}
		//creates the Frame components
		private JPanel createDefaultComponents(JFrame frame){
			JLabel nameLabel=FactoryFacade.makeLabels(" Title : ", smallFont);
			JTextField nameField=new JTextField(10);
			nameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			
			JLabel moneyLabel=FactoryFacade.makeLabels("Allocated Money : ", smallFont);
			JTextField moneyField=new JTextField(10);
			moneyField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			
			JButton continueButton=FactoryFacade.makeTextButtons("OK", nameLabel.getBackground(), Color.BLACK, smallFont);
			addDefaultActionListener(continueButton,frame,nameField,moneyField);
			
			return createDefaultLayout(constraintsToManipulate,nameLabel, nameField, moneyLabel, moneyField, continueButton);
		}
		//Adds the components to the frame
		private JPanel createDefaultLayout(GridBagConstraints c,JLabel nameLabel, JTextField nameField,JLabel moneyLabel,JTextField moneyField,JButton continueButton){
			JPanel panel=new JPanel();
			panel.setLayout(new GridBagLayout());
			FactoryFacade.addWithGridBag(1, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, nameLabel);
			FactoryFacade.addWithGridBag(2, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, nameField);
			FactoryFacade.addWithGridBag(1, 2, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, moneyLabel);
			FactoryFacade.addWithGridBag(2, 2, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, moneyField);
			FactoryFacade.addWithGridBag(2, 3, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.EAST, c, panel, continueButton);
			return panel;
		}
		//Adds the actionListener to the continueButton
		private void addDefaultActionListener(JButton continueButton,JFrame frame,JTextField nameField,JTextField moneyField){
			continueButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					try{
						//Gets user input and gets rid of frame
						frame.dispose();
						String title=nameField.getText();
						String money=moneyField.getText();
						money=money.replaceAll(",", "");
						double expense=Double.parseDouble(money);	
						//updates program with new information			
						resultSetFacade.updateString(2, title);
						resultSetFacade.updateDouble(4, expense);
						resultSetFacade.insertRow();
						personelDataSet.updateDouble(7, personelDataSet.getDouble(7)+expense); personelDataSet.updateRow();
						startUp.createBudgetFrame();
					}catch(NumberFormatException|SQLException e){
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Unable to complete transaction, you didn't enter a number","Error",JOptionPane.ERROR_MESSAGE);
					}
					selectedPanel=null;
				}		
			});
		}
		//Adds a windowListener that disposes when someone clicks on something other than the frame
		private void addWindowListener(JFrame frame){
			frame.addWindowListener(new WindowAdapter(){

				public void windowDeactivated(WindowEvent arg0) {
				frame.dispose();
					
				}
				
			});
		}
		//checks which JMenuItem was clicked
		private void checkPopUp(ActionEvent e) {
			if(e.getSource()==withdraw){	
				actOnWithdraw();
			}else if(e.getSource()==setTitle){
				actOnSetTitle();
			}else if(e.getSource()==setAllocatedMoney){
				actOnSetAllocatedMoney();
			}else if(e.getSource()==remove){
				actOnRemove();
			}
			
		}
		//Does the withdraw action by creating a JFrame which waits for user input
		private void actOnWithdraw(){		
			JFrame frame=new JFrame();
			frame.setAlwaysOnTop(true);
			frame.setResizable(false);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(300, 300);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			frame.setTitle("Withdraw");
			addWindowListener(frame);
			frame.add(createEnvelopeComponents(frame));
		}
		//creates the Frame components
		private JPanel createEnvelopeComponents(JFrame frame){
			JLabel nameLabel=FactoryFacade.makeLabels(" Amount : ", smallFont);
			JTextField nameField=new JTextField(10);
			nameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			
			JLabel descriptionLabel=FactoryFacade.makeLabels("Description : ", smallFont);
			JTextArea descriptionArea=new JTextArea(2,10);
			descriptionArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			descriptionArea.setLineWrap(true);
			descriptionArea.setWrapStyleWord(true);
			
			JButton continueButton=FactoryFacade.makeTextButtons("OK", nameLabel.getBackground(), Color.BLACK, smallFont);
			addEnvelopeActionListener(continueButton,frame,nameField,descriptionArea);
			
			return createEnvelopeLayout(constraintsToManipulate,nameLabel, nameField, descriptionLabel, descriptionArea, continueButton);
		}
		//Adds the components to the frame
		private JPanel createEnvelopeLayout(GridBagConstraints c,JLabel nameLabel, JTextField nameField,JLabel descLabel,JTextArea descArea,JButton continueButton){
			JPanel panel=new JPanel();
			panel.setLayout(new GridBagLayout());
			FactoryFacade.addWithGridBag(1, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, nameLabel);
			FactoryFacade.addWithGridBag(2, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, nameField);
			FactoryFacade.addWithGridBag(1, 2, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, descLabel);
			FactoryFacade.addWithGridBag(2, 2, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, descArea);
			FactoryFacade.addWithGridBag(2, 3, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.EAST, c, panel, continueButton);
			return panel;
		}
		//Adds the actionListener to the continueButton
		private void addEnvelopeActionListener(JButton continueButton,JFrame frame,JTextField field,JTextArea area){
			continueButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					try{
						//Gets user input and gets rid of frame
						frame.dispose();
						String expenseString=field.getText();
						expenseString=expenseString.replaceAll(",", "");
						double expense=Double.parseDouble(expenseString);
						String description=area.getText();
						//updates program with new information
						boolean okForTransaction=updateDataBaseWithdraw(expense);
						createReconciliation(description,expense,okForTransaction);
					}catch(NumberFormatException e){
						JOptionPane.showMessageDialog(null, "Unable to complete transaction, you didn't enter a number","Error",JOptionPane.ERROR_MESSAGE);
					}
					selectedPanel=null;
				}		
			});
		}
		
		//Adds a row to the Reconciliation Table
		private void createReconciliation(String description,double expense,boolean okForTransaction){
			ResultSet reconciliation=individualFac.getConnectionInfo("SELECT * from reconciliation");
			try {
				if(okForTransaction){
					reconciliation.moveToInsertRow();
					reconciliation.updateString(2, selectedPanel.getLabel());
					reconciliation.updateString(3, description);
					reconciliation.updateDouble(4, expense);
					reconciliation.updateString(5, selectedPanel.getExpenseType());
					reconciliation.insertRow();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//Updates the database with the values from the actOnWithdraw method
		private boolean updateDataBaseWithdraw(double expense){
			try {
				resultSetFacade.absolute(selectedPanel.getDataBaseIndex());
				double moneyDifference=resultSetFacade.getDouble(4)-resultSetFacade.getDouble(3)-expense;	
				if(moneyDifference>=0){
					resultSetFacade.updateDouble(3,resultSetFacade.getDouble(3)+expense);
					resultSetFacade.updateRow();
					personelDataSet.absolute(1);
					personelDataSet.updateDouble(5, personelDataSet.getDouble(5)+expense);
					personelDataSet.updateRow();
					startUp.createBudgetFrame();
					return true;
				}else{
					String formatDif=decimalFormat.format(Math.abs(moneyDifference));
					JOptionPane.showMessageDialog(null, "Unable to complete transaction, you are $"+formatDif+" over budget","Error",JOptionPane.ERROR_MESSAGE);
				}
			} catch (SQLException e) {e.printStackTrace();}
			return false;
		}
		//Does the setTitle action
		private void actOnSetTitle(){
			String title=JOptionPane.showInputDialog(null, "Type in the new title");
			if(title==null){
				return;
			}else if(title.equals("")){
				JOptionPane.showMessageDialog(null, "You didn't enter anything"); 
				return;
			}
			try {
				resultSetFacade.absolute(selectedPanel.getDataBaseIndex());
				updateDataBaseTitle(title);
				startUp.createBudgetFrame();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		//Updates the database with the values from the actOnSetTitle method
		private void updateDataBaseTitle(String title)throws SQLException{
			ResultSet reconciliationSet=individualFac.getConnectionInfo("SELECT * from reconciliation where label='"+resultSetFacade.getString(2)+"'");
			
			while(reconciliationSet.next()){
				reconciliationSet.updateString(2, title);
				reconciliationSet.updateRow();
			}
			resultSetFacade.updateString(2, title);
			resultSetFacade.updateRow();
		}
		//Does the setAllocatedMoney action
		private void actOnSetAllocatedMoney(){
			try{
				String allocatedMoneyString=JOptionPane.showInputDialog(null, "Type in the amount of allocated Money");
				allocatedMoneyString.replaceAll(",", "");
				double allocatedMoneyDouble=0;
				allocatedMoneyDouble=Double.parseDouble(allocatedMoneyString);
				if(checkForUpdate(allocatedMoneyDouble)) updateDataBaseMoney(allocatedMoneyDouble);
			} catch (SQLException e) {
				e.printStackTrace();
			}catch(NumberFormatException e){
				JOptionPane.showMessageDialog(null, "You didn't enter a number","Error",JOptionPane.ERROR_MESSAGE); return;
			}catch(NullPointerException e){
				return;
			}	
		}
		//Checks if it is clear to update the dataBase
		private boolean checkForUpdate(double allocatedMoneyDouble) throws SQLException{
				resultSetFacade.absolute(selectedPanel.getDataBaseIndex());
				if(allocatedMoneyDouble>=resultSetFacade.getDouble(3)){
					//Checks if income is greater than total expenses
					if(personelDataSet.getDouble(4)>=personelDataSet.getDouble(7)+allocatedMoneyDouble-resultSetFacade.getDouble(4)){
						return true;
					}else{
					JOptionPane.showMessageDialog(null, "The projected expenses from your envelopes are more than your income"); 	
					}
				}else{
					JOptionPane.showMessageDialog(null, "The money spent is more than the money allocated by $"+decimalFormat.format(allocatedMoneyDouble)+", delete some bank transactions or allocate more money"); 	
				}
			return false;
		}
		//Updates the database with the values from the actOnSetAllocatedMoney method
		private void updateDataBaseMoney(double allocatedMoney)throws SQLException{
				
			personelDataSet.absolute(1);
			personelDataSet.updateDouble(7, personelDataSet.getDouble(7)+allocatedMoney-resultSetFacade.getDouble(4));
			personelDataSet.updateRow();
			
			resultSetFacade.absolute(selectedPanel.getDataBaseIndex());
			resultSetFacade.updateDouble(4, allocatedMoney);
			resultSetFacade.updateRow();
		
			startUp.createBudgetFrame();
				
		}
		//Does the remove action
		private void actOnRemove(){
			int hitYes=JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this envelope, this will also delete the transactions associated with it","Confirmation",JOptionPane.INFORMATION_MESSAGE);
			if(hitYes==0){
				try {
					updateDataBaseRemove();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		//updates the dataBaseTable for actOnRemove
		private void updateDataBaseRemove() throws SQLException{
			resultSetFacade.absolute(selectedPanel.getDataBaseIndex());
			//Updates Reconciliation
			ResultSet recSet=individualFac.getConnectionInfo("SELECT * from reconciliation where label='"+resultSetFacade.getString(2)+"'");
			while(recSet.next()){
			recSet.deleteRow();
			}		
			//Updates personel_data
			personelDataSet.absolute(1);
			personelDataSet.updateDouble(5, personelDataSet.getDouble(5)-resultSetFacade.getDouble(3));
			personelDataSet.updateDouble(7, personelDataSet.getDouble(7)-resultSetFacade.getDouble(4));

			personelDataSet.updateRow();
			//Updates envelope
			resultSetFacade.deleteRow();
			startUp.createBudgetFrame();
		}
	}
}// End of BudgetCenter






//TODO     START OF RECONCILIATION

class ReconciliationCenter extends centerPanel {
	private ArrayList<RecInstance> save = new ArrayList<RecInstance>();
	private ArrayList<RecInstance> variable = new ArrayList<RecInstance>();
	private ArrayList<RecInstance> fixed = new ArrayList<RecInstance>();
	private FrameFacade individualFac;
	private Color bluePanelColor=new Color(153,204,255);
	private Color redPanelColor=new Color(255,153,153);
	private Color greenPanelColor=new Color(153,255,153);
	private double savingSpent,fixedSpent,variableSpent;
	private ButtonListener buttonListener=new ButtonListener();
	private int dataBaseIndex=1;
	private ResultSet personelDataSet;
	// calls other methods that will manipulate returnPanel
	public JScrollPane makeCenterPanel(FrameFacade individualFac) {
		this.individualFac = individualFac;
		getConInfo();
		createSmallerComponent();
		yPos=1;
		return addScrollPane();
	}
	// Gets the connection from the FactoryFac and passes the info to createInstance()
	protected void getConInfo() {
		personelDataSet=individualFac.getConnectionInfo("SELECT * From personel_data");
		resultSetFacade=individualFac.getConnectionInfo("SELECT * From reconciliation");
		setConInfo();
		getDefaultMoneyValues();
		addDefaultInstance();
	}
	private void setConInfo(){
		try {
			while(resultSetFacade.next()){
				RecInstance instance=new RecInstance(resultSetFacade.getString(3),resultSetFacade.getString(5),resultSetFacade.getString(2),resultSetFacade.getDouble(4),dataBaseIndex);
				addEnvelopeInstance(instance);
				dataBaseIndex++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//Gets the moneyRatios for the saving,fixed,and variable
	protected void getDefaultMoneyValues(){
		for(RecInstance instance:save){
			savingSpent+=instance.getMoneySpent();
		}
		for(RecInstance instance:variable){
			variableSpent+=instance.getMoneySpent();
		}
		for(RecInstance instance:fixed){
			fixedSpent+=instance.getMoneySpent();
		}
	}
	//Adds instances of the BudgetTemplateTabs
	protected void addDefaultInstance(){
		//String description, String expenseType, String label, double allocatedMoney
		save.add(0,new RecInstance("","Save","Savings : ",savingSpent,1));
		fixed.add(0,new RecInstance("","Fixed","Fixed  : ",fixedSpent,1));
		variable.add(0,new RecInstance("","Variable","Variable:",variableSpent,1));
	}
	//Adds instances of the Envelopes
	protected void addEnvelopeInstance(RecInstance instance) {
		String type=instance.getExpenseType();
		if(type.equals("Save")){
			save.add(instance);
		}else if(type.equals("Variable")){
			variable.add(instance);
		}else if(type.equals("Fixed")){
			fixed.add(instance);
		}
	}
	// Uses the connection info and ArrayList to create the panels inside the centerPanel
	protected void createSmallerComponent() {
		returnPanel.setLayout(new GridBagLayout());
		makeDefaults(fixed.get(0),bluePanelColor);
		for (int x = 1; x < fixed.size(); x++) {
			makeEnvelopes(fixed.get(x),bluePanelColor,"Withdraw");
		}
		makeDefaults(variable.get(0),redPanelColor);
		for (int x = 1; x < variable.size(); x++) {
			makeEnvelopes(variable.get(x),redPanelColor,"Withdraw");
		}
		makeDefaults(save.get(0),greenPanelColor);
		for (int x = 1; x < save.size(); x++) {
			makeEnvelopes(save.get(x),greenPanelColor,"Deposit");
		}
	}

	// Creates the envelopes that are a part of the Fixed type
	protected void makeEnvelopes(RecInstance RInstance,Color color,String transactionType) {
		//initialize Envelope Panels
		JPanel envelope=new JPanel();
		envelope.setLayout(new GridBagLayout());
		envelope.setBackground(color);
		//Create components
		JButton remove=FactoryFacade.makeIconButtons("C:/Users/Kyle Peters/Documents/picturesForFinance/deleteButton.png", color,50,50);
		remove.addActionListener(buttonListener);
		RInstance.setButton(remove);

		JLabel title=FactoryFacade.makeLabels(RInstance.getLabel()+" : "+RInstance.getDescription(), smallFont);
		JLabel transaction=FactoryFacade.makeLabels(Double.toString(RInstance.getMoneySpent())+" "+transactionType,smallFont);
		setEnvelopesLayout(constraintsToManipulate,envelope,remove,title,transaction);
	}
	//Adds the components of makeEnvelope to returnPanel
	protected void setEnvelopesLayout(GridBagConstraints c,JPanel panel,JButton remove,JLabel title,JLabel transaction){
		FactoryFacade.addWithGridBag(1, 1, 1, 2, 75, 75, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, panel, remove);
		FactoryFacade.addWithGridBag(2, 1, 1, 1, 100, 100, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, panel, title);
		FactoryFacade.addWithGridBag(2, 2, 1, 1, 100, 100, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, panel, transaction);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));

		FactoryFacade.addWithGridBag(1,yPos,1,1,50,50, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, returnPanel, panel);
		yPos++;
		
	}
	// Creates the default envelope for Fixed
	protected void makeDefaults(RecInstance RInstance,Color color) {
		//initialize defaultPanels 
		JPanel defaultPanels=new JPanel();
		defaultPanels.setLayout(new GridBagLayout());
		defaultPanels.setBackground(color);
		//create Components
		JButton add=FactoryFacade.makeIconButtons("C:/Users/Kyle Peters/Documents/picturesForFinance/addButton.png", color,50,50);
		add.addActionListener(buttonListener);
		RInstance.setButton(add);

		JLabel title=FactoryFacade.makeLabels(RInstance.getLabel()+" "+"Total : $"+RInstance.getMoneySpent(), largeFont);
		setDefaultsLayout(constraintsToManipulate,defaultPanels,add,title);
	}
	//Adds the default Reconciliation panels to a panel
	protected void setDefaultsLayout(GridBagConstraints c,JPanel defaultPanels,JButton add,JLabel title){
		FactoryFacade.addWithGridBag(1, 1, 1, 1, 50, 50, GridBagConstraints.BOTH, GridBagConstraints.WEST, c, defaultPanels, add);
		FactoryFacade.addWithGridBag(2, 1, 1, 1, 100, 100, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, defaultPanels, title);
		defaultPanels.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
		
		FactoryFacade.addWithGridBag(1, yPos, 1, 1, 50, 50, GridBagConstraints.BOTH, GridBagConstraints.CENTER, c, returnPanel, defaultPanels);
		yPos++;
	}
	//TODO Start of Reconciliation ActionListener
	class ButtonListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			if(checkAdd(e)){
			
			}else{
				checkRemove(e);
			}

		}
		//Checks if the default panels threw the action
		private boolean checkAdd(ActionEvent e) {
			if(e.getSource()==save.get(0).getButton()){
				withdrawFromRec("Save");
				return true;
			}else if(e.getSource()==fixed.get(0).getButton()){
				withdrawFromRec("Fixed");
				return true;
			}else if(e.getSource()==variable.get(0).getButton()){
				withdrawFromRec("Variable");
				return true;
			}
			return false;
		}
		//Creates the Frame that appears when you click the defaultPanel' add button
		private void withdrawFromRec(String type){		
			JFrame frame=new JFrame();
			frame.setResizable(false);
			frame.setAlwaysOnTop(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(500, 300);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			frame.setTitle("Withdraw");
			addWindowListener(frame);
			frame.add(createFrameComponents(frame,type));
		}
		
		
		//creates the Frame components
		private JPanel createFrameComponents(JFrame frame,String type){
			JLabel warning=new JLabel("This function should only be used in emergencies or when depositing");			
			JLabel warning2=new JLabel("unexpected savings, everyday expenses should use the envelopes");
			JLabel nameLabel=FactoryFacade.makeLabels(" Amount : ", smallFont);
			JTextField nameField=new JTextField(10);
			nameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			
			JLabel descriptionLabel=FactoryFacade.makeLabels("Description : ", smallFont);
			JTextArea descriptionArea=new JTextArea(3,10);
			descriptionArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			descriptionArea.setLineWrap(true);
			descriptionArea.setWrapStyleWord(true);
			
			JButton continueButton=FactoryFacade.makeTextButtons("OK", nameLabel.getBackground(), Color.BLACK, smallFont);
			addEnvelopeActionListener(type,continueButton,frame,nameField,descriptionArea);
			
			return createFrameLayout(constraintsToManipulate,warning,warning2,nameLabel, nameField, descriptionLabel, descriptionArea, continueButton);
		}
		//Adds the components to the frame
		private JPanel createFrameLayout(GridBagConstraints c,JLabel warning,JLabel warning2,JLabel nameLabel, JTextField nameField,JLabel descLabel,JTextArea descArea,JButton continueButton){
			JPanel panel=new JPanel();
			panel.setLayout(new GridBagLayout());
			FactoryFacade.addWithGridBag(1, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, nameLabel);
			FactoryFacade.addWithGridBag(2, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, nameField);
			FactoryFacade.addWithGridBag(1, 2, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, descLabel);
			FactoryFacade.addWithGridBag(2, 2, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c, panel, descArea);
			FactoryFacade.addWithGridBag(1, 3, 2, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.SOUTH, c, panel, warning);
			FactoryFacade.addWithGridBag(1, 4, 2, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.NORTH, c, panel, warning2);
			FactoryFacade.addWithGridBag(2, 5, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.EAST, c, panel, continueButton);
			return panel;
		}
		
		
		//Adds the actionListener to the continueButton
		private void addEnvelopeActionListener(String type,JButton continueButton,JFrame frame,JTextField field,JTextArea area){
			continueButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					try{
						//Gets user input and gets rid of frame
						frame.dispose();
						String expenseString=field.getText();
						expenseString=expenseString.replaceAll(",", "");
						double expense=Double.parseDouble(expenseString);
						String description=area.getText();
						//updates program with new information
						insertTransaction(type,expense,description);
						startUp.createRecFrame();
					}catch(NumberFormatException e){
						JOptionPane.showMessageDialog(null, "Unable to complete transaction, you didn't enter a number","Error",JOptionPane.ERROR_MESSAGE);
					}
				}	
				private void insertTransaction(String type,double expense,String description){
					try {
						resultSetFacade.moveToInsertRow();
						resultSetFacade.updateString(2, "Default");
						resultSetFacade.updateString(3, description);
						resultSetFacade.updateDouble(4, expense);
						resultSetFacade.updateString(5, type);
						resultSetFacade.insertRow();
						personelDataSet.absolute(1);
						
						updateFinancials(expense);	
						
						personelDataSet.updateRow();
						resultSetFacade.close();
						personelDataSet.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				//Updates labels and part of the dataBase
				private void updateFinancials(double expense) throws SQLException{
					double newWealth=0;
					double wealthChange=0;
					if(type.equals("Save")){
						newWealth=personelDataSet.getDouble(3)+expense;
						wealthChange=personelDataSet.getDouble(2)+expense;
					}else{
						newWealth=personelDataSet.getDouble(3)-expense;
						wealthChange=personelDataSet.getDouble(2)-expense;
					}
					System.out.println(newWealth+" "+wealthChange);
					personelDataSet.updateDouble(3, newWealth);
					personelDataSet.updateDouble(2, wealthChange);
					TopPanel.getInstance(null).setWealth(newWealth);
					TopPanel.getInstance(null).setChange(wealthChange);
				}
			});
		}
		//Adds a listener to tell if the user clicked somewhere other than the Frame
		private void addWindowListener(JFrame frame){
			frame.addWindowListener(new WindowAdapter(){

				public void windowDeactivated(WindowEvent arg0) {
				frame.dispose();
					
				}
				
			});
		}
		//Checks to see if the transaction panel threw the event
		private void checkRemove(ActionEvent e) {
			for (int x = 1; x < save.size(); x++) {
				if(e.getSource()==save.get(x).getButton()){
					showConfirmPopup(save.get(x));
					return;
				}
			}for (int x = 1; x < fixed.size(); x++) {
				if(e.getSource()==fixed.get(x).getButton()){
					showConfirmPopup(fixed.get(x));
					return;
				}
			}for (int x = 1; x < variable.size(); x++) {
				if(e.getSource()==variable.get(x).getButton()){
					showConfirmPopup(variable.get(x));
					return;
				}
			}
		}
		//Makes sure the user wants to delete this object
		private void showConfirmPopup(RecInstance RInstance){
			int hitYes=JOptionPane.showConfirmDialog(null, "Are you sure you wish to continue, this will also delete the withdraw from the envelopes");
			if(hitYes==0){
				deleteTransaction(RInstance);
				startUp.createRecFrame();
			}
		}
		//deletes the Rec panel from the database
		private void deleteTransaction(RecInstance RInstance){
			try {
				resultSetFacade.absolute(RInstance.getDataBaseIndex());
				if(!RInstance.getLabel().equals("Default")){
					updateEnvelopeGeneratedPanel(RInstance);
				}else{
					updateRecGeneratedPanel(RInstance);
				}
				resultSetFacade.deleteRow();
				resultSetFacade.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
		//updates DataBase due to a rec. panel being deleted that was created by using withdraw in an envelope
		private void updateEnvelopeGeneratedPanel(RecInstance RInstance) throws SQLException{
			personelDataSet.absolute(1);
			//updates overall money spent
			personelDataSet.updateDouble(5, personelDataSet.getDouble(5)-RInstance.getMoneySpent());
			personelDataSet.updateRow();
			//updates envelope money spent
			ResultSet envelopeSet=individualFac.getConnectionInfo("SELECT * from envelope where label='"+RInstance.getLabel()+"'");		
			envelopeSet.next();
			envelopeSet.updateDouble(3, envelopeSet.getDouble(3)-RInstance.getMoneySpent());
			envelopeSet.updateRow();		
		}
		//updates DataBase due to  a rec. panel being deleted that was created by using plus on a default rec. Panel
		private void updateRecGeneratedPanel(RecInstance RInstance) throws SQLException{
			personelDataSet.absolute(1);
			double newWealth=0,wealthChange=0;
			if(RInstance.getExpenseType().equals("Save")){
				newWealth=personelDataSet.getDouble(3)-RInstance.getMoneySpent();
				wealthChange=personelDataSet.getDouble(2)-RInstance.getMoneySpent();
			}else{
				newWealth=personelDataSet.getDouble(3)+RInstance.getMoneySpent();
				wealthChange=personelDataSet.getDouble(2)+RInstance.getMoneySpent();
			}
			personelDataSet.updateDouble(3, newWealth);
			personelDataSet.updateDouble(2, wealthChange);
			TopPanel.getInstance(null).setWealth(newWealth);
			TopPanel.getInstance(null).setChange(wealthChange);
			personelDataSet.updateRow();
		}
	}
}// End of ReconciliationCenter
//TODO     START OF CHARTS
class ChartsCenter extends centerPanel{
	private FrameFacade individualFac;
	private DecimalFormat decimalFormat=new DecimalFormat("#.#");
	private ArrayList<ChartMakerClass> chartHolder=new ArrayList<ChartMakerClass>();
	private int total=0;
	
	// calls other methods that will manipulate returnPanel
	public JScrollPane makeCenterPanel(FrameFacade individualFac) {
		this.individualFac = individualFac;
		getConInfo();
		createCenterPanel();
		return addScrollPane();
	}
	//Adds the charts to one frame
	private void createCenterPanel(){
		int x=1,y=1;
		returnPanel.setBackground(new Color(153,204,255));
		returnPanel.setLayout(new GridBagLayout());
		for(ChartMakerClass chart:chartHolder){
			setSizeOfCharts(chart);
			FactoryFacade.addWithGridBag(x, y, 1, 1, 50, 50, GridBagConstraints.BOTH, GridBagConstraints.CENTER, constraintsToManipulate, returnPanel, chart);
			if(x==2){
				y++;
				x=0;
			}
			x++;
		}

	}
	private void setSizeOfCharts(JComponent chart){
		Dimension d=new Dimension(320,350);
		chart.setPreferredSize(d);
		chart.setMaximumSize(d);
		chart.setMinimumSize(d);
	}
	// Gets the connection from the InvestFac and passes the info to createInstance()
	protected void getConInfo(){
		resultSetFacade=individualFac.getConnectionInfo("SELECT * From envelope");
		ArrayList<ChartInstance> sliceHolder=new ArrayList<ChartInstance>();
		try {
			getPresentChart(sliceHolder);
			getPastCharts();
			resultSetFacade.close();
		} catch (SQLException e) {}	
	}
	//Gets the info from the current month
	private void getPresentChart(ArrayList<ChartInstance> sliceHolder) throws SQLException{
		total=findTotal(resultSetFacade);
		while(resultSetFacade.next()){
			double value=resultSetFacade.getDouble(3);
			double percent=value/total*100;
			if(percent<1)continue;
			sliceHolder.add(new ChartInstance((int)value ,resultSetFacade.getString(2)+" ("+decimalFormat.format(percent)+"%)"));
		}
		createSmallerComponent(sliceHolder,"Current expenses",total);
	}
	private int findTotal(ResultSet resultSetFacade) throws SQLException{
		int total=0;
		resultSetFacade.absolute(0);
		while(resultSetFacade.next()){
			total+=resultSetFacade.getDouble(3);
		}
		resultSetFacade.absolute(0);
		return total;
	}
	private int findTotal(String[] values){
		int total = 0;
		for(int x=0;x<values.length;x+=2){
			total+=Double.parseDouble(values[x+1]);
		}
		return total;
	}
	//Gets the charts stored in the dataBase
	private void getPastCharts() throws SQLException{
		ResultSet pastCharts=individualFac.getConnectionInfo("SELECT * From Charts");
		//Gets the resultSet to one after the last element
		pastCharts.last(); pastCharts.next();
		while(pastCharts.previous()){
			ArrayList<ChartInstance> tempValueHolder=new ArrayList<ChartInstance>();
			String[] values=pastCharts.getString(3).split(",");
			total=findTotal(values);
			for(int x=0;x<values.length;x+=2){
				
				double intValue=Double.parseDouble(values[x+1]);
				double percent=intValue/total*100;
				if(percent<1)continue;
				ChartInstance i=new ChartInstance((int)intValue,values[x]+" ("+decimalFormat.format(percent)+"%)");
				tempValueHolder.add(i);
			}
			createSmallerComponent(tempValueHolder,pastCharts.getInt(2)+" months ago",total);
		}
	}
	//Sorts the sliceHolder ArrayList into descending order
	private void sortConInfo(ArrayList<ChartInstance>list){
		int tempValue=0;
		String tempName="";
		for(int i = 0; i< list.size(); i++)
		{ 
		    for(int j = 0; j< list.size()-1; j++)
		    {
		        if(list.get(j+1).getValue()>list.get(j).getValue())
		        {
		            tempValue=list.get(j+1).getValue();
		            tempName=list.get(j+1).getName();
		            list.get(j+1).setValue(list.get(j).getValue());
		            list.get(j).setValue(tempValue);
		            list.get(j+1).setName(list.get(j).getName());
		            list.get(j).setName(tempName);
		        }
		    }
		}
	}
	// Uses the connection info and ArrayList to create the panels inside the centerPanel
	protected void createSmallerComponent(ArrayList<ChartInstance> list,String title,int total) {
		sortConInfo(list);
		chartHolder.add(new ChartMakerClass(list,total,title));
	}
}// End of ChartsCenter
//paints the charts on the screen
//TODO ChartMakerClass Start

//Creates the charts and stores it as an object in this class
class ChartMakerClass extends JComponent{
	private Color[] colorArray={Color.BLUE,Color.RED,Color.YELLOW,Color.ORANGE,Color.GREEN,Color.PINK,Color.CYAN,Color.BLACK,Color.MAGENTA};
	public ArrayList<ChartInstance> sliceHolder=new ArrayList<ChartInstance>();
	private int total=0;
	private Point middlePiePoint=new Point(255,205);
	private Point2D middleSlicePoint;
	private boolean isLastIndex=false;
	private String title;
	//creates the charts
	public ChartMakerClass(ArrayList<ChartInstance> sliceHolder,int total,String title){
		this.sliceHolder=sliceHolder;
		this.total=total;
		this.title=title;
	}
	//Draws the pie graph
	public void paint(Graphics g){
		Graphics2D graphics=(Graphics2D)g;
		graphics.setStroke(new BasicStroke(3));
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if(total>0){
			int curveAmount=drawPieThrough9(graphics);
			if(sliceHolder.size()>9){
				drawPieAfter9(graphics,curveAmount);
			}
		}else{
			drawArcNoValues(graphics);
		}
		drawTitle(graphics);
	}
	//Draws an ac when there are no expenses available
	private void drawArcNoValues(Graphics2D graphics) {
		Arc2D arc= new Arc2D.Double(150, 100, 210, 210, 0, 360, Arc2D.PIE);
		graphics.setColor(Color.GRAY);
		graphics.fill(arc);
		
	}
	//Draws the Titles of the Charts
	private void drawTitle(Graphics2D graphics) {
		int xPos=(int) (middlePiePoint.getX()-(title.length()*3));
		graphics.setFont(new Font("Serif",Font.BOLD,15));
		graphics.setColor(Color.BLACK);
		graphics.drawString(title, xPos, 40);
		
	}
	//draws the 'other' pie slice after 9 slices
	private void drawPieAfter9(Graphics2D graphics,int curveAmount) {
		int valueLeft=0;
		for(int x=9;x<sliceHolder.size();x++){
			valueLeft+=sliceHolder.get(x).getValue();
		}
		int startAngle=(int)(curveAmount*360/total);
		int endAngle=(int) (valueLeft*360/total);
		Arc2D arc= new Arc2D.Double(150, 100, 210, 210, startAngle, endAngle, Arc2D.PIE);
		graphics.setColor(Color.GRAY);
		graphics.fill(arc);
		middleSlicePoint=findMiddlePoint(startAngle,endAngle);
		drawLabelLines(graphics);
		drawText(graphics,"Other");
	}
	//Draws first 9 slices
	private int drawPieThrough9(Graphics2D graphics){
		int startAngle = 0,endAngle=0,curveAmount=0;
		for(int x=0;x<sliceHolder.size();x++){
			if(x==sliceHolder.size()-1){
				isLastIndex=true;
			}
			if(x==9){
				break;
			}
			startAngle=(int)(curveAmount*360/total);
			endAngle=(int) (sliceHolder.get(x).getValue()*360/total);
			
			Arc2D arc= new Arc2D.Double(150, 100, 210, 210, startAngle, endAngle, Arc2D.PIE);
			graphics.setColor(colorArray[x]);
			graphics.fill(arc);
			curveAmount+=sliceHolder.get(x).getValue();
			
			middleSlicePoint=findMiddlePoint(startAngle,endAngle);
			
			drawLabelLines(graphics);
			drawText(graphics,sliceHolder.get(x).getName());		
		}
		return curveAmount;
	}
	
	//Draws the Pie Slice label
	private void drawText(Graphics2D graphics,String label) {
		double xDistanceScaled=modifyXPosLabel(label);
		double yDistanceScaled=modifyYPosLabel();
		graphics.drawString(label, (int)(middleSlicePoint.getX()+xDistanceScaled), (int)(middleSlicePoint.getY()+yDistanceScaled));
		
	}
	//Changes the double that affects where the label is written on the X axis
	private double modifyXPosLabel(String label){
		double xDistanceScaled=(middleSlicePoint.getX()-middlePiePoint.getX());
		//Far right slices
		if(xDistanceScaled>50){
			xDistanceScaled/=6;
		//Far left slices
		}else if(xDistanceScaled<-50){
			xDistanceScaled/=10;
			xDistanceScaled-=label.length()*6;
		//Close to CenterX
		}else if(xDistanceScaled<49&&xDistanceScaled>-49){
			xDistanceScaled/=3;
			xDistanceScaled-=label.length()*3;		
		}else{
			xDistanceScaled/=2;
		}
		return xDistanceScaled;
	}
	//Changes the double that affects where the label is written on the Y axis
	private double modifyYPosLabel(){
		double yDistanceScaled=(middleSlicePoint.getY()-middlePiePoint.getY());
		//Top slices
		if(yDistanceScaled<-50){
			yDistanceScaled/=6;
		//bottomSlices
		}else if(yDistanceScaled>50){
			yDistanceScaled/=4;
		//Slices close to center but below the center line
		}else if(yDistanceScaled<20&&yDistanceScaled>0){
			yDistanceScaled/=2;
		}else{
			yDistanceScaled/=4;
		}
		return yDistanceScaled;
	}
	//Finds the Mid point of each slice
	private Point2D findMiddlePoint(int startAngle,int endAngle) {
		Arc2D middlePointArc= new Arc2D.Double(150,100,210,210,startAngle,endAngle/2,Arc2D.OPEN);
		middleSlicePoint=middlePointArc.getEndPoint();
		return middleSlicePoint;
	}
	//Draws the lines that connect the pie slice with its label
	public void drawLabelLines(Graphics2D graphics){
		graphics.setColor(Color.BLACK);
		int scaledXDistance=(int) ((middleSlicePoint.getX()-middlePiePoint.getX())/9);
		int scaledYDistance=(int) ((middleSlicePoint.getY()-middlePiePoint.getY())/9);
		graphics.drawLine((int)middleSlicePoint.getX(), (int)middleSlicePoint.getY(), (int)middleSlicePoint.getX()+scaledXDistance, (int)middleSlicePoint.getY()+scaledYDistance);
	}
}





