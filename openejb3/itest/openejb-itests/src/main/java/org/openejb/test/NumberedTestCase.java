package org.openejb.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;
/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class NumberedTestCase extends Assert implements Test{
    
    Method[] testMethods = new Method[]{};
    protected static final String standardPrefix = "test##_";
    
    class MethodComparator implements java.util.Comparator {
        
        public int compare(Object o1, Object o2){
                Method m1 = (Method)o1;
                Method m2 = (Method)o2;
                return m1.getName().compareTo(m2.getName());
        }
        public boolean eqauls(Object other){
            if(other instanceof MethodComparator)
                return true;
            else
                return false;
        }
    }
                        

    public NumberedTestCase(){
        try{
            // Get all methods of the subclass
            Method[] methods = getClass().getMethods();
            java.util.TreeSet tm = new java.util.TreeSet(new MethodComparator());

            // Add the ones that start with "test"
            for (int i=0; i < methods.length; i++){
                if (methods[i].getName().startsWith("test")){
                    tm.add(methods[i]);
                }
            }
            testMethods = new Method[tm.size()];
            Iterator orderedMethods = tm.iterator();
            for(int i=0;orderedMethods.hasNext();i++){
                testMethods[i]=(Method)orderedMethods.next();
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    
    protected void setUp() throws Exception{
    }

    protected void tearDown() throws Exception{
    }
    
    /**
     * Counts the number of test cases that will be run by this test.
     */
    public int countTestCases() {
        return testMethods.length;
    }
    
    /**
     * Runs a test and collects its result in a TestResult instance.
     */
    public void run(TestResult result) {
        try{
            setUp();
        } catch (Exception e){
            Test test = new Test(){
                    public int countTestCases() {
                        return 0;
                    }
                    public void run(TestResult result) {
                    }
                    public String toString(){
                        return name()+".setUp()";
                    }
                };
        
            result.addError(test, e);
            return;
        }
        for (int i=0; i < testMethods.length; i++){
            run(result, testMethods[i]);
        }
        try{
            tearDown();
        } catch (Exception e){
            Test test = new Test(){
                    public int countTestCases() {
                        return 0;
                    }
                    public void run(TestResult result) {
                    }
                    public String toString(){
                        return name()+".tearDown()";
                    }
                };
        
            result.addError(test, e);
            return;
        }
    }

    protected void run(final TestResult result, final Method testMethod) {
        Test test = createTest(testMethod);
        result.startTest(test);
        Protectable p= new Protectable() {
            public void protect() throws Throwable {
                runTestMethod(testMethod);
            }
        };
        result.runProtected(test, p);
        result.endTest(test);
    }
    

    protected Test createTest(final Method testMethod){
        Test test = new Test(){
            public int countTestCases() {return 1;}
            public void run(TestResult result) {}
            public String toString(){
                return createTestName(testMethod);
            }
        };
        return test;
    }

    protected void runTestMethod(Method testMethod) throws Throwable {
        try {
            testMethod.invoke(this, new Class[0]);
        } catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            e.fillInStackTrace();
            throw e;
        }
    }


    public String toString(){
        return name();
    }

    public String name(){
        return "";
    }
    
    protected String createTestName(Method testMethod){
        return name() + removePrefix(testMethod.getName());
    }

    protected static String removePrefix(String name){
        return removePrefix(standardPrefix, name);
    }
    
    protected static String removePrefix(String prefix, String name){
        return name.substring(prefix.length());
    }
}


