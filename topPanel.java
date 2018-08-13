import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import javax.swing.*;
//Used SingleTon because all four frames use the same topPanel
class TopPanel{
	protected Font regFont = new Font("Serif", Font.BOLD, 30);
	private Color backgroundPanelColor = new Color(0, 128, 255);
	protected static ResultSet resultSetFacade;
	protected JPanel returnPanel;
	private DecimalFormat decimalFormat=new DecimalFormat("###,###.##");
	protected GridBagConstraints constraintsToManipulate = new GridBagConstraints();
	
	private String wealthChange,wealth;
	private long daysLeft;
	private Calendar startDate = Calendar.getInstance();
	private Box wealthBox;
	private static TopPanel singleInstance;
	private static FrameFacade individualFac;
	private JLabel wealthChangeLabel;

	private TopPanel(){}
	//Only returns one instance
	public static TopPanel getInstance(FrameFacade individualFacade){
		if(singleInstance==null){
			individualFac=individualFacade;
			singleInstance=new TopPanel();
		}
		return singleInstance;
	}
	public JPanel makePanel(){
		if(returnPanel==null){
			returnPanel=new JPanel();
			makeTopPanel();
		}
		return returnPanel;
	}
	public void setWealth(double wealth){
		((JLabel)wealthBox.getComponent(1)).setText("Wealth : "+decimalFormat.format(wealth));
	}
	public void setChange(double wealthChange){
		wealthChangeLabel.setText("Wealth Change : "+decimalFormat.format(wealthChange));
	}
	// Calls the methods that manipulate the returnPanel and returns the
	// returnPanel
	private JPanel makeTopPanel() {
		
		getConInfo();
		createTopComponents();
		
		return returnPanel;
	}

	// Gets the connection from the FactoryFacade
	private void getConInfo() {
		resultSetFacade = individualFac.getConnectionInfo("SELECT * From personel_data");
		try {
			if (resultSetFacade.next()) {
				
				Calendar endCal=Calendar.getInstance();
				endCal.setTime(resultSetFacade.getDate(6));
				daysLeft=ChronoUnit.DAYS.between(startDate.toInstant(), endCal.toInstant());	
				
				wealth =decimalFormat.format(resultSetFacade.getDouble(3));
				wealthChange =decimalFormat.format(resultSetFacade.getDouble(2));
			}
		} catch (SQLException e) {}
	}
	// Creates the components for the topPanel
	private void createTopComponents() {
		returnPanel.setLayout(new GridBagLayout());
		returnPanel.setBackground(backgroundPanelColor);
		
		wealthBox=createEditBoxes("Wealth : $" + wealth+"|");
		addListenerToWealth((JButton)wealthBox.getComponent(0),(JLabel)wealthBox.getComponent(1));
		wealthChangeLabel = FactoryFacade.makeLabels("Change : $" + wealthChange+"|", regFont);
		//moneyRatioLabel = FactoryFacade.makeLabels("$" + moneySpent + " / $" + totalMoney+"|", regFont);
		Box daysLeftBox=createEditBoxes(daysLeft + " Days Left");
		addListenerToDaysLeft((JButton)daysLeftBox.getComponent(0),(JLabel)daysLeftBox.getComponent(1));

		createTopPanel(wealthBox, wealthChangeLabel,daysLeftBox,constraintsToManipulate);
	}
	// Puts the topPanel together
	private void createTopPanel(Box wealthBox, JLabel wealthChangeLabel,Box daysLeftBox, GridBagConstraints c) {
		FactoryFacade.addWithGridBag(1, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c,
				returnPanel, wealthBox);
		FactoryFacade.addWithGridBag(2, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c,
				returnPanel, wealthChangeLabel);
		//FactoryFacade.addWithGridBag(3, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c,
		//		returnPanel, moneyRatio);
		FactoryFacade.addWithGridBag(4, 1, 1, 1, 50, 50, GridBagConstraints.NONE, GridBagConstraints.CENTER, c,
				returnPanel, daysLeftBox);
	}
	//Creates the Boxes that contain the edit button and a JLabel created with the String parameter
	private Box createEditBoxes(String labelName){
		JButton button = FactoryFacade.makeIconButtons(
				"C:/Users/Kyle Peters/Documents/picturesForFinance/editButton.png", backgroundPanelColor,20,30);
		JLabel label = FactoryFacade.makeLabels(labelName,regFont);
		Box returnBox=Box.createHorizontalBox();
		returnBox.add(button);
		returnBox.add(label);
		return returnBox;
	}
	//Enables the person's wealth to be changed
	private void addListenerToWealth(JButton wealthButton,JLabel wealthLabel){
		wealthButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try{
					String wealthString=JOptionPane.showInputDialog(null,"You don't have to fill in this field if you prefer not to","Set Wealth",JOptionPane.INFORMATION_MESSAGE);
					wealthString=wealthString.replaceAll(",", "");
					double wealthDouble=Double.parseDouble(wealthString);
					updateDataBase(wealthDouble);
					updateLabels(wealthDouble);
				}catch(NumberFormatException | NullPointerException | SQLException exception){
				}	
			}	
			private void updateDataBase(Double wealthDouble) throws SQLException{
				resultSetFacade.absolute(1);
				resultSetFacade.updateDouble(3,wealthDouble);
				resultSetFacade.updateRow();
			}
			private void updateLabels(Double wealthDouble) throws SQLException{
				String formatString=decimalFormat.format(wealthDouble);
				wealthLabel.setText("Wealth : $" + formatString+" | ");
				wealth=formatString;
			}
		});//End of ActionListener
	}
	//Enables the person to change the end of the Budget Month
	private void addListenerToDaysLeft(JButton daysLeftButton,JLabel daysLeftLabel){
		daysLeftButton.addActionListener(new ActionListener(){
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				try{
					int hitYes=JOptionPane.showConfirmDialog(null,"Do you wish to start a new Month","Set Date",JOptionPane.INFORMATION_MESSAGE);
					if(hitYes==0){
						int month=(startDate.get(Calendar.MONTH)==12)?1:startDate.get(Calendar.MONTH)+1;			
						java.sql.Date endOfNewMonth=new java.sql.Date(startDate.get(Calendar.YEAR)-1900,month,startDate.get(Calendar.DAY_OF_MONTH));
						updateDataBase(endOfNewMonth);		
						updateLabels();
					}
				}catch(NullPointerException |SQLException exception){}	
			}
			private void updateDataBase(java.sql.Date endOfNewMonth) throws SQLException{
				resultSetFacade.absolute(1);
				resultSetFacade.updateDate(6, endOfNewMonth);
				resultSetFacade.updateRow();
			}
			private void updateLabels() throws SQLException{
				Calendar endCal=Calendar.getInstance();
				endCal.setTime(resultSetFacade.getDate(6));
				daysLeft=ChronoUnit.DAYS.between(startDate.toInstant(), endCal.toInstant());		
				daysLeftLabel.setText(daysLeft + " Days Left");
			}
		});//End of Buttonistener
	}
}// End of BudgetTop

