# Budget-App
  This application helps people plan for their fixed cost, variables costs, and savings by using zero based budgeting.
  
  This application uses the abstract factory design pattern to create the top panel and center panel for each of the three tabs.  The topPanel.java file creates the top panel file and the centerPanel.java file creates the center panel.  The Facade design pattern was also used to provide helper functions for the entire application in the FrameFacade.java file.  The AbstractFactorGui.java file was used to set up the GUI and the ProjectFrame.java file combined the center and top panels together.  The startUp file contained the main method, and the BudgetInstance.java file is used to store data within the app for all three of the tabs.
