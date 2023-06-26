package org.uu3.sparql;

import ca.uhn.fhir.context.FhirContext;
import org.fhir.ucum.UcumException;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.utils.FHIRPathEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SparqlR4Test {
	private static final FhirContext ourCtx = FhirContext.forR4Cached();
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SparqlR4Test.class);
	private static FHIRPathEngine ourEngine;

	@Test
	public void testAs() {
		Observation obs = new Observation();
		obs.setValue(new StringType("FOO"));

		// Allow for bad casing on primitive type names - this is a common mistake and
		// even some 4.0.1 SPs use it

		List<Base> value = ourCtx.newFhirPath().evaluate(obs, "Observation.value.as(string)", Base.class);
		assertEquals(1, value.size());
		assertEquals("FOO", ((StringType) value.get(0)).getValue());

		value = ourCtx.newFhirPath().evaluate(obs, "Observation.value.as(FHIR.string)", Base.class);
		assertEquals(1, value.size());
		assertEquals("FOO", ((StringType) value.get(0)).getValue());

		value = ourCtx.newFhirPath().evaluate(obs, "Observation.value.as(String)", Base.class);
		assertEquals(1, value.size());
		assertEquals("FOO", ((StringType) value.get(0)).getValue());

		value = ourCtx.newFhirPath().evaluate(obs, "Observation.value.as(FHIR.String)", Base.class);
		assertEquals(1, value.size());
		assertEquals("FOO", ((StringType) value.get(0)).getValue());
	}

	@Test
	public void testComponentCode() {
		String path = "(Observation.component.value.ofType(FHIR.Quantity)) ";

		Observation o1 = new Observation();
		o1.addComponent()
			.setCode(new CodeableConcept().addCoding(new Coding().setSystem("http://foo").setCode("code1")))
			.setValue(new Quantity().setSystem("http://bar").setCode("code1").setValue(200));
		o1.addComponent()
			.setCode(new CodeableConcept().addCoding(new Coding().setSystem("http://foo").setCode("code2")))
			.setValue(new Quantity().setSystem("http://bar").setCode("code2").setValue(200));

		List<Base> outcome = ourCtx.newFhirPath().evaluate(o1, path, Base.class);
		assertEquals(2, outcome.size());

	}

	@BeforeAll
	public static void beforeClass() {
		ourEngine = new FHIRPathEngine(new HapiWorkerContext(ourCtx, ourCtx.getValidationSupport()));
	}
}
