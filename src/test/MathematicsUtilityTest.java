package test;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MathematicsUtilityTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Inside setUpBeforeClass:::");
	}
	

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Inside tearDownAfterClass:::");
	}

	@Before
	public void setUp() throws Exception {
		System.out.println("Inside setUp:::");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Inside tearDown:::");
	}

	@Test
	public void testLog2Succeess() {
		fail("Not yet implemented");
	}
	@Test
	public void testLog2Failure() {
		fail("Not yet implemented");
	}

}
