package org.uu3.sparql;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.FhirPathExecutionException;
import org.hl7.fhir.exceptions.PathEngineException;
import org.hl7.fhir.r5.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Quantity;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.utils.FHIRPathEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SparqlR5Test {
	private static final FhirContext ourCtx = FhirContext.forR5Cached();
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(SparqlR5Test.class);
	private static FHIRPathEngine ourEngine;

	class E extends RuntimeException {}; // short name

	@Test
	public void testAs() {
		class PathException extends RuntimeException {
			public PathException(String path, Exception ex){
				super("Error executiong \"" + path + "\" - " + ex.getMessage(), ex);
			}
		}

		Observation obs = new Observation();
		obs.setValue(new StringType("FOO"));

		// Allow for bad casing on primitive type names - this is a common mistake and
		// even some 4.0.1 SPs use it

		String As_string = "Observation.value.as(string)";
		String As_String = "Observation.value.as(String)";
		String As_FHIR_string = "Observation.value.as(FHIR.string)";
		String As_FHIR_String = "Observation.value.as(FHIR.String)";

		List<String> paths = new ArrayList<>(List.of(
			As_String,
			As_string,
			As_FHIR_string,
			As_String,
			As_FHIR_String,
			As_string,
			As_FHIR_string
		));
		List<RuntimeException> errors = new ArrayList<>();
		for (int i = 0; i < paths.size(); ++i) {
			String path = paths.get(i);
			try {
				testPath(obs, path);
			} catch (RuntimeException ex) {
				errors.add(new RuntimeException("eval " + i + " \"" + path + "\" - " + ex.getMessage(), ex));
			}
		}
		System.out.println(" ".join(errors.stream().map(e -> e.getMessage()).toString()));
		assertEquals(new ArrayList<>(), errors);
	}

	private static List<Base> testPath(Observation obs, String thePath) {
		List<Base> value = ourCtx.newFhirPath().evaluate(obs, thePath, Base.class);
		assertEquals(1, value.size());
		assertEquals("FOO", ((StringType) value.get(0)).getValue());
		return value;
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
