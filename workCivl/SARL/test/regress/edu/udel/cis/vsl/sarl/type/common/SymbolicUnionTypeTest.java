package edu.udel.cis.vsl.sarl.type.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicIntegerType.IntegerKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicRealType.RealKind;
import edu.udel.cis.vsl.sarl.number.IF.Numbers;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.object.IF.Objects;

/**
 * @author jthakkar
 *
 */
/**
 * @author jthakkar
 *
 */
public class SymbolicUnionTypeTest {

	/**
	 * an ObjectFactory object that is used to instantiate the variables that is
	 * unionType and unionType2.
	 */
	ObjectFactory objectFactory;
	/**
	 * a numberFactory is used to instantiate the objectFactory
	 */
	NumberFactory numberFactory;

	/**
	 * creating unionType to be used for tests
	 */
	CommonSymbolicUnionType unionType, unionType2;

	/**
	 * an array of SymbolicTypes to be used in creating the SequenceType
	 */
	CommonSymbolicType typesArray[];

	/**
	 * integer types to be used in creating the SequenceType
	 */
	CommonSymbolicIntegerType idealIntKind, boundedIntKind;

	/**
	 * real types to be used in creating the SequenceType
	 */
	CommonSymbolicRealType idealRealKind, floatRealKind;

	/**
	 * typeSequence is used to assign the array of CommonSymbolicType to the
	 * variables that is unionType and unionType2.
	 */
	CommonSymbolicTypeSequence typeSequence;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		numberFactory = Numbers.REAL_FACTORY;
		objectFactory = Objects.newObjectFactory(numberFactory);

		idealIntKind = new CommonSymbolicIntegerType(IntegerKind.IDEAL);
		boundedIntKind = new CommonSymbolicIntegerType(IntegerKind.BOUNDED);
		idealRealKind = new CommonSymbolicRealType(RealKind.IDEAL);
		floatRealKind = new CommonSymbolicRealType(RealKind.FLOAT);
		typesArray = new CommonSymbolicType[4];

		// an array of CommonSymbolicType
		typesArray[0] = idealRealKind;
		typesArray[1] = floatRealKind;
		typesArray[2] = idealIntKind;
		typesArray[3] = boundedIntKind;

		typeSequence = new CommonSymbolicTypeSequence(typesArray);

		// initialization of all the variables.
		unionType = new CommonSymbolicUnionType(
				objectFactory.stringObject("myUnion"), typeSequence);
		unionType2 = new CommonSymbolicUnionType(
				objectFactory.stringObject("myUnion2"), typeSequence);
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * in this test the hash code for two variables is compared with each other
	 * and depending upon the variable they should be either similar or
	 * different.
	 */
	@Test
	public void testComputeHashCode() {
		assertNotEquals(unionType.computeHashCode(),
				unionType2.computeHashCode());
	}

	/**
	 * this test checks whether a variable of one type is the same or different
	 * from a variable of another type.
	 */
	@Test
	public void testTypeEquals() {
		assertFalse(unionType.typeEquals(unionType2));
	}

	/**
	 * this test checks the name assigned to each variable with other variables.
	 */
	@Test
	public void testName() {
		// System.out.println(unionType.name());
		// System.out.println(unionType2.name());
		assertNotEquals(unionType.name(), unionType2.name());
	}

	/**
	 * this test checks the typeSequence assigned to each variable.
	 */
	@Test
	public void testSequence() {
		// System.out.println(unionType.sequence());
		// System.out.println(unionType.sequence());
		assertEquals(unionType.sequence(), unionType2.sequence());
	}

	/**
	 * here the pureType is always returned as null until something is not
	 * explicitly assigned.
	 */
	@Test
	public void testPureType() {
		assertNull(unionType.getPureType());
		unionType.setPureType(unionType);
		assertNotNull(unionType.getPureType());
	}

	/**
	 * this test checks the string output of a variable. The string output of
	 * variables of the same type should match but those of different types
	 * should not.
	 */
	@Test
	public void testToStringBuffer() {

		// System.out.println(unionType.toStringBuffer(true));
		// System.out.println(unionType.toStringBuffer(false));
		// System.out.println(unionType2.toStringBuffer(true));
		// System.out.println(unionType2.toStringBuffer(false));
		assertEquals(unionType.toStringBuffer(true).toString(),
				"Union[myUnion,<real,float,int,bounded>]");
		assertEquals(unionType.toStringBuffer(false).toString(),
				"Union[myUnion,<real,float,int,bounded>]");
		assertEquals(unionType2.toStringBuffer(false).toString(),
				"Union[myUnion2,<real,float,int,bounded>]");
		assertEquals(unionType2.toStringBuffer(true).toString(),
				"Union[myUnion2,<real,float,int,bounded>]");
		assertNotEquals(unionType.toStringBuffer(true).toString(),
				"Union[myUnion2,<real,float,int,bounded>]");
		assertNotEquals(unionType.toStringBuffer(false).toString(),
				"Union[myUnion2,<real,float,int,bounded>]");
		assertNotEquals(unionType2.toStringBuffer(true).toString(),
				"Union[myUnion,<real,float,int,bounded>]");
		assertNotEquals(unionType2.toStringBuffer(false).toString(),
				"Union[myUnion,<real,float,int,bounded>]");

	}

}
