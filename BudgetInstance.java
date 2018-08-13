import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
// This class holds the envelope data
public class BudgetInstance {
	private double moneySpent, totalMoney;
	private String expenseType, label;
	private JButton button;
	private JLabel envelopeExpense;
	private int dataBaseIndex;
	public BudgetInstance(double moneySpent, double totalMoney, String expenseType, String label, int dataBaseIndex) {
		this.moneySpent = moneySpent;
		this.totalMoney = totalMoney;
		this.expenseType = expenseType;
		this.label = label;
		this.dataBaseIndex=dataBaseIndex;
	}

	public double getMoneySpent() {
		return moneySpent;
	}

	public void setMoneySpent(double moneySpent) {
		this.moneySpent = moneySpent;
	}

	public double getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(double totalMoney) {
		this.totalMoney = totalMoney;
	}

	public String getExpenseType() {
		return expenseType;
	}

	public void setExpenseType(String expenseType) {
		this.expenseType = expenseType;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	public JButton getButton() {
		return button;
	}

	public void setButton(JButton button) {
		this.button = button;
	}

	public int getDataBaseIndex() {
		return dataBaseIndex;
	}
	public JLabel getEnvelopeExpense() {
		return envelopeExpense;
	}

	public void setEnvelopeExpense(JLabel envelopeExpense) {
		this.envelopeExpense = envelopeExpense;
	}
}// End of BudgetInstance

// This class holds all of the expense data
class RecInstance {
	private String description,expenseType,label;
	private double moneySpent;
	private JButton button;
	private int dataBaseIndex;
	public RecInstance(String description, String expenseType, String label, double moneySpent,int dataBaseIndex) {
		super();
		this.description = description;
		this.expenseType = expenseType;
		this.label = label;
		this.moneySpent = moneySpent;
		this.dataBaseIndex=dataBaseIndex;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getExpenseType() {
		return expenseType;
	}
	public void setExpenseType(String expenseType) {
		this.expenseType = expenseType;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getMoneySpent() {
		return moneySpent;
	}

	public void setMoneySpent(double moneySpent) {
		this.moneySpent = moneySpent;
	}
	public JButton getButton() {
		return button;
	}
	public void setButton(JButton button) {
		this.button = button;
	}
	public int getDataBaseIndex() {
		return dataBaseIndex;
	}
}// End of RecInstance

class ChartInstance{
	private int value;
	private String name;
	public ChartInstance(int value, String name) {
		this.value = value;
		this.name = name;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
}