## Text Parser

###### Text Parser has been developed for automating the process of common text manipulation and has been internally designed for supporting flexible implementation of new text manipulation modules.

###### The 4 steps below are to be followed for defining and registering a new module to the application:

###### As of TextParser 3.0, all steps below are to be implemented in "ModuleFactory.java". No Module related changes should need to be made anywhere outside of "ModuleFactory.java"
###### Refer to the included Class Diagram and Sequence Diagram for additional understanding of the logic flow.

#### Step (1)

    This step implements the logic of the desired module.
    See example code snippet below for line numbers.

     Line 1.        Instantiate a static final object of an anonymous subclass of the abstract class "Module" inside the "Modules" static class.
    
     Line 3.        Override abstract method "replace" to define your own implementation of the module.
                    *** Unless you are implementing step (3) below, you do not need to and you should not do anything with "moduleCtx".
		        You however still need to include this parameter in the signature of "replace" function.
			See step (3) for details of this parameter.
    
     Lines 5-6	Add as many calls as needed to "addReplaceable()" function
                    for implementing replacement logic.
                    This can be achieved through specifying on-the-fly regexp-replacement pair as in line 5
                    or specifying a function (method) to be called for complex replacement logic as in line 6. 
                    In line 6, the syntax in setMethod (s -> replacementFunction(s)) is for creating a lambda function
                    to encapsulate the custom created function.
                    Replacement is performed in the same order as calls are made to "addReplaceable()".
                    i.e., the output of each replacement is fed as an input to the next replacement.
    
     Lines 9-11	Define the body of the custom function(s) in line 6. 


```
    1.	public static final Module SAMPLEANALYZE = new Module()
    2.	{
    3.		protected void replace(Module.ModuleContext moduleCtx)
    4.		{
    5.			addReplaceable(new Replaceable("<<Regular Expression pattern to look for>>","<<Replacement Text>>"));
    			AND/OR
    6.			addReplaceable(new Replaceable().setMethod(c -> replacementFunction(c)));
    			:::
    7.		}
    8.
    9.		private String replacementFunction(Module.ModuleContext moduleCtx)
    10.		{
                        String input = moduleCtx.getInputString();
    			::::	//This is your own code. Do whatever is needed to transform the input String and return the result String back to the caller.
    11.		}
    12.	}
```

#### Step (2) (Optional)

     Step 2 is to add any needed list of submenus on top of the target menu items
       
     See example code snippet below for line numbers.
     
     Lines 3-4.      Call addSubmenu function
                     for every needed submenu.
     
                     Parameters of addSubmenu:
     
                     1. submenuName                     -       String
                     2. parentMenuName (Optional)       -       String        - The parent of the submenu to be created.
                                                                                Default is "Modules" menu.
                                                                                If a parent menu is specified
                                                                                , it has to have been created
                                                                                using same function (addSubmenu)



#### Step (3) (Optional)

     Step 3 involves defining a method (function) which is automatically called at runtime to perform any desired GUI input operation ahead of the text replacement.
     An example is if you require to get an user input parameter or list of parameterss from the user so it can be used to perform the analysis.
     
     See example code snippet below for line numbers.
     
     There are some set of rules that need to be followed when defining this method:
          1. LINE 1.   Define your new method as a LAMBDA so you can pass it on to your Module in step (4)
                       after the module has been registered (See step 4 for details).
          2.           Either define your new method and store it in a static final "Module.Displayable" reference so other developers can reuse it if needed
                       , or use a pre-defined static final "Module.Displayable" LAMBDA that has been defined by another developer.
          3. LINE 26.  For the replacement operation to execute, this function has to return an object of data type "Module.ModuleContext".
                       This object is in charge of storing and carrying all key-value pairs
       	               that represent the inputs from the current method to the replacement Module.
		       It also isolates execution level module bound parameter data from the Module object
		       , so the module can be considered by the developer as clean from old data when reused across multiple executions.
             LINE 24.  To construct an instance of ModuleContext, use initContext() method from the module instance.
             LINE 12.   In situations when you do not want the replacement to execute under certain conditions (e.g.,  when user hits "Cancel" in the popup input GUI)
                       , then simply return null, and that will prevent the registered Module from running any replacements at runtime.
	  
######  The Following snippet is a different version of SAMPLEDISPLAYABLE. Please refer to the source code for the updated version.

`````
     1.	private static final Module.Displayable SAMPLEDISPLAYABLE = (Module module, java.awt.Component parent)->{
     2.		JTextField inputField = new JTextField();
     3.		Object[] message = {"Enter Integer, Integer List and/or Integer Range:",inputField};
     4.		int intVal = -1;
     5.		while(true)
     6.		{
     7.			try
     8.			{
     9.				int response = JOptionPane.showConfirmDialog(parent, message, "Title", JOptionPane.OK_CANCEL_OPTION);
    10.				if(response != JOptionPane.OK_OPTION)
    11.				{
    12.					return null;
    13.				}
    14.				intVal = Integer.parseInt(inputField.getText());
    15.			}
    16.			catch(Exception e)
    17.			{
    18.				continue;
    19.			}
    20.			if(intVal < 0)
    21.			{
    22.				continue;
    23.			}
    24.			Module.ModuleContext moduleCtx = module.initContext();
    25.			moduleCtx.put("intVal", intVal);
    26.			return moduleCtx;
    27.		}
    28.	};
`````



#### Step (4)

    This step registers the previously created logic (Module object) to the GUI (TextParser)
    See example code snippet below for line numbers.
   
    Lines 5 and 12.    Call registerModule function
                       to register the previously created Module instant to the application GUI.
   		
                       "ModuleRegistrant" class is in charge of creating the GUI components
                       of the module and linking them to the Module.
	       
                       Parameters of registerModule:
   
                                Parameter Name                Type		
                                1. submenuName (Optional)  -  String                -  The name  of the submenu
                                                                                       to add the target module menu item.
                                                                                       This requires that a submenu should be added
                                                                                       using addSubmenu method.
                                2. isDefaultModule         -  boolean (true/false)  -  whether this is the default module
                                                                                       when the program loads.
                                                                                       Specify only one default module.
                                3. module                  -  Module                -  The static "Module" object
                                                                                       which was created in step 1.
                                4. moduleName              -  String                -  The name of the module as desired
                                                                                       to be used in various parts of the GUI.
                                5. promptText              -  String                -  The prompt string to be initially displayed
                                                                                       in the Paste area when the module is selected.
                                6. aboutText               -  String                -  A one-liner description of the module
                                                                                       to be displayed in the "About" dialog 
     
   
    LINE 19 (optional).   If any of the registered modules require input from the user as explained in step (3),
                           then call setPromptDisplayMethod to assign your LAMBDA from step (3) to the registered Module.
    LINE 20 (Optional).   If it is desired to disable autoscrolling to the bottom of the text field
                           (in case an important header/banner is displayed at the top), use disableAutoScrollDown method.
    LINE 27 (Optional).   If it is desired to enable File read support so files can be browsed and opened in addition to the option to use COPY-PASTE
                            , use enableFileReadSupport call to enable this feature.
#####			   ** IT IS IMPORTANT that you do NOT use the static references of Modules in ModuleFactory
#####			   (like ModuleFactory.SAMPLEDISPLAYABLE) to call setPromptDisplayMethod and enableFileReadSupport.
#####			   Doing so will throw an exception to protect these references from developer changes during reuse
#####			   , so they can always be used by other modules.
#####			   Instead, use the return from registerModuler to call setPromptDisplayMethod and enableFileReadSupport.
#####			   What registerModule returns is a deep-clone of the static module, so the static module will stay untouched.
#####                      Also, use only the return from registerModuler when calling disableAutoScrollDown and enableFileReadSupport (if needed).
     
					
                        
```
     1.	public static void register(TextParser.ModuleRegistrant moduleRegistrant)
     2.	{
     3.		moduleRegistrant.addSubmenu("MENU1");
     4.		moduleRegistrant.addSubmenu("MENU2","MENU1");
     5.		moduleRegistrant.registerModule("MENU2",
     6.				true
     7.				, ModuleFactory.Modules.SAMPLEANALAYZE
     8.				, "Sample Analyzer"
     9.				, "Please use CTRL+V or Edit menu to paste text..."
    10.				, "Performs Text Analysis ..");
    11.				
    12.		Module module2 = moduleRegistrant.registerModule("MENU2",
    13.				false
    14.				, ModuleFactory.Modules.SAMPLEANALYZE2
    15.				, "Sample Analyzer 2"
    16.				, "Please use CTRL+V or Edit menu to paste text..."
    17.				, "Performs Text Analysis ...");
    18.		
    19.	module2.setPromptDisplayMethod(ModuleFactory.Displayables.SAMPLEDISPLAYABLE);
    20.         moduleRegistrant.disableAutoScrollDown(module2);
:::
    27.          moduleRegistrant.enableFileReadSupport(module2);
    28.	}
```

#####                      The method enableFileReadSupport has an optional boolean parameter (canBeCancelled) for enabling the option of
#####                       allowing the user to cancel a Module execution while an opened file stream is being accessed.
#####                      The following line will result in enabling File Read support but give no option for the user to cancel started module excutions.

```
    27.          moduleRegistrant.enableFileReadSupport(module2, false);
```

#####                      The default of This option is true and it is made available to avoid user waiting
#####                       as a result of inadvertently selecting incorrect/large file. 

#####                      ** Also, please note when File Read Support is enabled and when the user opts to select a file as opposed to PASTing the text
#####                       , this will result in the inputString parameter that is bound to ModuleContext object being NULL.
#####                      Instead, Module.ModuleContext encapsulates a BufferedReader which can be used to read the input file a line at a time.
#####                      As a developer, it is important to evaluate whether input String is NULL or whether input Reader is NULL.
#####                      See below snippet for example.


    LINE 14.              Local variable reader is being iniitialized with the encapsulated reader.
                          If the user inputs the data from an input file:
                              1. getReader will return a non-NULL value containing the BufferedReader.
                              AND
                              2. getInputString will return NULL
                          If the user uses COPY and PASTE to input the data:
                              1. getReader will return NULL
                              AND
                              2. getInputString will return a non-NULL value with the pasted String.
			      
     Lines 15-23.         Check which option was picked by the User and for standardizing the processing of either the input String or the input Reader
                          , input String is wrapped in a BufferedReader and processReader Consumer is called to process the input in either of the two cases.
			  
			  
#####                     ** When File Read Support is enabled on a Module, the Module cannot start with a Replaceable that is not calling setMethod.
#####                     This is because specifying regexp-replacement pair is not possible when each line in the file reader has to be processed individually.
#####                     i.e., If we were to swap lines 7. and 8. below, an exception would be thrown everytime the user inputs data thru a file.
     
     Line 8.              The result of the first Replaceable can then be fed into a regexp-replacement that does not specify setMethod, since the input is now
                          a String.  

			  

```
     1. public static final Module SAMPLEANALYZER = new Module()
     2. {
     3.      
     4.      protected void replace(Module.ModuleContext moduleCtx)
     5.      {
     6.           
     7.           addReplaceable(new Replaceable().setMethod(s->performAction(s)));
     8.           addReplaceable(new Replaceable(("[ ]+[\r\n$]"),"\n"));
     9.      }

    10.  private String performAction(Module.ModuleContext moduleCtx)
    11.  {
               :::
    12.        Consumer<BufferedReader> processReader = (BufferedReader reader)->{
                     :::
    13.        };
               :::
    14.        BufferedReader reader = moduleCtx.getReader();
    15.        if(reader == null)
    16.        {
    17.              BufferedReader stringReader = new BufferedReader(new StringReader(moduleCtx.getInputString()));
    18.              processReader.accept(stringReader);
    19.        }
    20.        else
    21.        {
    22.               processReader.accept(reader);
    23.        }
```


                        


