import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.*;


public abstract class FrameFacade {
	//Create the SQLConnection
	
	protected static DataBase dataBase=new DataBase();
	
	// Creates a connection with MySQL
	protected static Connection SQLConnection=dataBase.getConnection();
	//sets project to a new month
	protected abstract void setNewMonth();
	
	//allows program to execute a query on the SQLDataBase
	public ResultSet getConnectionInfo(String query){
		return dataBase.getResultSet(SQLConnection,query);
	}
	//Checks if a full month has passed
	protected boolean checkMonth(){
		ResultSet personelDataSet=getConnectionInfo("SELECT * from personel_data");
		Calendar startDate=Calendar.getInstance();
		Calendar endDate=Calendar.getInstance();
		java.sql.Date date=null;
		try {
			personelDataSet.absolute(1);
			date = personelDataSet.getDate(6);
			personelDataSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		endDate.setTime(date);
		long daysLeft=ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant());
		return daysLeft<=0;
	}

	
}// End of FrameFacade



class BudgetFacade extends FrameFacade {

	//One connection is for the topPanel and one for the centerPanel
	public BudgetFacade(){
		if(checkMonth()){
			setNewMonth();
		}
	}
	//sets project to a new month
	protected void setNewMonth(){
		try{
			Calendar startDate=Calendar.getInstance();
			int month=(startDate.get(Calendar.MONTH)==12)?1:startDate.get(Calendar.MONTH)+1;			
			@SuppressWarnings("deprecation")
			java.sql.Date endOfNewMonth=new java.sql.Date(startDate.get(Calendar.YEAR)-1900,month,startDate.get(Calendar.DAY_OF_MONTH));
			updateDate(endOfNewMonth);		
			updateFinancials();

		}catch(NullPointerException |SQLException exception){}	
	}
	
	private void updateFinancials() throws SQLException {
		ResultSet personelDataSet=getConnectionInfo("SELECT * from personel_data");
		personelDataSet.absolute(1);
		double moneySaved=getMoneySaved(personelDataSet);
		personelDataSet.updateDouble(5, 0);
		personelDataSet.updateDouble(3, personelDataSet.getDouble(3)+moneySaved);
		personelDataSet.updateDouble(2, personelDataSet.getDouble(2)+moneySaved);
		personelDataSet.updateRow();
		
	}
	private double getMoneySaved(ResultSet personelDataSet) throws SQLException{
		double moneySaved=personelDataSet.getDouble(4)-personelDataSet.getDouble(5);
		ResultSet ResultSetEnvelope=getConnectionInfo("SELECT * from envelope where label='Save'");
		while(ResultSetEnvelope.next()){
			moneySaved+=ResultSetEnvelope.getDouble(3);
		}
		return moneySaved;
	}
	private void updateDate(java.sql.Date endOfNewMonth) throws SQLException{
		ResultSet personelDataSet=getConnectionInfo("SELECT * from personel_data");
		personelDataSet.absolute(1);
		personelDataSet.updateDate(6, endOfNewMonth);
		personelDataSet.updateRow();
	}
	public boolean checkIncomeVsExpense(ResultSet resultSet){
		double income=0;
		double expenses=0;
		try {
			income = resultSet.getDouble(4);
			expenses=resultSet.getDouble(5);
		} catch (SQLException e){
			e.printStackTrace();
		}
		if(income<expenses){
			return true;
		}else{
			return false;
		}
	}
}// End of BudgetFacade











class RecFacade extends FrameFacade {
	//One connection is for the topPanel and one for the centerPanel
	public RecFacade(){
		if(checkMonth()){
			//setNewMonth();
		}
	}
	//sets project to a new month
	protected void setNewMonth(){
		ResultSet recSet=getConnectionInfo("SELECT * from reconciliation");
		try {
			while(recSet.next()){
				recSet.deleteRow();
			}
			recSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}// End of RecFacade


class ChartsFacade extends FrameFacade {
	//One connection is for the topPanel and one for the centerPanel
	public ChartsFacade(){
		if(checkMonth()){
			setNewMonth();
			JOptionPane.showMessageDialog(null, "A New Month has Began");
		}
	}
	//sets project to a new month
	protected void setNewMonth(){
		ResultSet chartSet=getConnectionInfo("SELECT * from charts");
		ResultSet envelopeSet=getConnectionInfo("SELECT * from envelope");
		StringBuilder sqlString=new StringBuilder();
		try {
			getNewTextChart(sqlString,envelopeSet);
			insertNewInteger(chartSet);
			addNewChart(sqlString,chartSet);
			updateEnvelope(envelopeSet);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Creates the String to be added to the dataBase
	private void getNewTextChart(StringBuilder sqlString,ResultSet envelopeSet) throws SQLException{
		while(envelopeSet.next()){
			if(sqlString.capacity()<sqlString.length()+20)sqlString.ensureCapacity(sqlString.capacity()+50);
			sqlString.append(envelopeSet.getString(2)+",");
			sqlString.append(envelopeSet.getDouble(3)+",");
		}
		sqlString.deleteCharAt(sqlString.length()-1);
		System.out.println(sqlString.capacity()+" "+sqlString.length());
	}
	//Updates the Integer used to tell how many months ago this was
	private void insertNewInteger(ResultSet chartSet) throws SQLException{
		while(chartSet.next()){
			int newInt= chartSet.getInt(2)+1;
			if(newInt<=12){
				chartSet.updateInt(2,newInt);
				chartSet.updateRow();
			}else{
				chartSet.deleteRow();
			}
		}
	}
	//inserts a new chart into the database
	private void addNewChart(StringBuilder sqlString,ResultSet chartSet) throws SQLException{
		chartSet.moveToInsertRow();
		chartSet.updateInt(2, 1);
		chartSet.updateString(3, sqlString.toString());
		chartSet.insertRow();
		
	}
	//Updates the values in the envelope table
		private void updateEnvelope(ResultSet envelopeSet) throws SQLException {
			envelopeSet.absolute(0);
			while(envelopeSet.next()){
				envelopeSet.updateDouble(3,0);
				envelopeSet.updateRow();
			}
			
		}
}// End of ChartsFacade

class FactoryFacade{
	private static NumberFormat decimalFormat=NumberFormat.getCurrencyInstance();
	// Adds components to a Panel
	private FactoryFacade() {
	}
	public static JSlider makeSliders(int min,int max,int start,Color color){
		 try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			JSlider slider=new JSlider(min,max,start);
			slider.setEnabled(false);
			slider.setForeground(Color.BLACK);
			slider.setBackground(color);
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			return slider;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		 return null;
	}
	public static void addWithGridBag(int x, int y, int width, int height, int weightX, int weightY, int fill,
			int anchor, GridBagConstraints constraints, JPanel p, JComponent comp) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.weightx = weightX;
		constraints.weighty = weightY;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.fill = fill;
		constraints.anchor = anchor;

		p.add(comp, constraints);
	}
	// Makes the buttons for the GUI
	public static JButton makeIconButtons(String label, Color color,int width,int height) {
		JButton returnButton = new JButton();
		returnButton.setIcon(getImageIcon(label,width,height));
		returnButton.setBorderPainted(false);
		returnButton.setFocusPainted(false);
		returnButton.setBackground(color);
		return returnButton;
	}
	public static JButton makeTextButtons(String label, Color background,Color foreground,Font font) {
		JButton returnButton = new JButton(label);
		returnButton.setBorderPainted(false);
		returnButton.setFocusPainted(false);
		returnButton.setBackground(background);
		returnButton.setForeground(foreground);
		returnButton.setFont(font);
		return returnButton;
	}
	public static ImageIcon getImageIcon(String destination,int width,int height){
		Image image=Toolkit.getDefaultToolkit().getImage(destination);
		image=image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		ImageIcon icon=new ImageIcon(image);
		return icon;
	}
	// Makes the Labels for the GUI
	public static JLabel makeLabels(String label, Font font) {
		JLabel returnLabel = new JLabel(label);
		returnLabel.setFont(font);
		return returnLabel;
	}
	public static double formatNumbers(double numberToFormat){
		decimalFormat.format(numberToFormat);
		return numberToFormat;
	}
}// End of FrameFacade


class DataBase{
	private Connection connection;
	//Gets the connection
	public Connection getConnection(){
		if(connection!=null){
			return connection;
		}
		try {
			String forName="com.mysql.jdbc.Driver";
			Class.forName(forName);
			String database="jdbc:mysql://localhost/finance_Data";
			String user="root";
			String password="Coke6008337";
			connection=DriverManager.getConnection(database,user,password);
			return connection;
		}catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ResultSet getResultSet(Connection con, String query) {
		ResultSet resultSet=null;
		try {
			Statement statement=con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			resultSet=statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return resultSet;
	}
}//End of DataBase
