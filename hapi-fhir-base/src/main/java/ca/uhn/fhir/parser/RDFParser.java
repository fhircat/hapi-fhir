/*-
 * #%L
 * HAPI FHIR - Core Library
 * %%
 * Copyright (C) 2014 - 2023 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ca.uhn.fhir.parser;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeDeclaredChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeChildContainedResources;
import ca.uhn.fhir.context.RuntimeChildDirectResource;
import ca.uhn.fhir.context.RuntimeChildExtension;
import ca.uhn.fhir.context.RuntimeChildNarrativeDefinition;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.util.rdf.RDFUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.irix.IRIs;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBackboneElement;
import org.hl7.fhir.instance.model.api.IBaseDatatypeElement;
import org.hl7.fhir.instance.model.api.IBaseElement;
import org.hl7.fhir.instance.model.api.IBaseExtension;
import org.hl7.fhir.instance.model.api.IBaseHasExtensions;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IBaseXhtml;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.instance.model.api.INarrative;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ca.uhn.fhir.context.BaseRuntimeElementDefinition.ChildTypeEnum.ID_DATATYPE;
import static ca.uhn.fhir.context.BaseRuntimeElementDefinition.ChildTypeEnum.PRIMITIVE_DATATYPE;
import static ca.uhn.fhir.context.BaseRuntimeElementDefinition.ChildTypeEnum.PRIMITIVE_XHTML;
import static ca.uhn.fhir.context.BaseRuntimeElementDefinition.ChildTypeEnum.PRIMITIVE_XHTML_HL7ORG;

/**
 * This class is the FHIR RDF parser/encoder. Users should not interact with this class directly, but should use
 * {@link FhirContext#newRDFParser()} to get an instance.
 */
public class RDFParser extends BaseParser {

	private static final String VALUE = "v";
	private static final String FHIR_INDEX = "index";
	private static final String FHIR_PREFIX = "fhir";
	private static final String FHIR_NS = "http://hl7.org/fhir/";
	private static final String RDF_PREFIX = "rdf";
	private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String RDFS_PREFIX = "rdfs";
	private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
	private static final String XSD_PREFIX = "xsd";
	private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";
	private static final String SCT_PREFIX = "sct";
	private static final String SCT_NS = "http://snomed.info/id#";
	private static final String EXTENSION_URL = "url";
	private static final String ELEMENT_EXTENSION = "extension";

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RDFParser.class);

	public static final String NODE_ROLE = "nodeRole";
	private static final List<String> ignoredIriPredicates = Arrays.asList(RDF.type.getURI(), FHIR_NS + NODE_ROLE);
	private static final List<String> ignoredLiteralPredicates = Arrays.asList(FHIR_NS+FHIR_INDEX);
	public static final String TREE_ROOT = "treeRoot";
	public static final String RESOURCE_ID = "id";
	public static final String ID = "id";
	public static final String ELEMENT_ID = "id";
	public static final String DOMAIN_RESOURCE_CONTAINED = "contained";
	public static final String EXTENSION = "extension";
	public static final String CONTAINED = "contained";
	public static final String MODIFIER_EXTENSION = "modifierExtension";
	private final Map<Class, String> classToFhirTypeMap = new HashMap<>();
	protected String[] primitiveTypes = {"base64Binary", "boolean", "canonical", "code", "date", "dateTime", "decimal", "id", "instant", "integer", "integer64", "markdown", "oid", "string", "positiveInt", "time", "unsignedInt", "uri", "url", "uuid"};

	private final Lang lang;

	/**
	 * Do not use this constructor, the recommended way to obtain a new instance of the RDF parser is to invoke
	 * {@link FhirContext#newRDFParser()}.
	 *
	 * @param parserErrorHandler the Parser Error Handler
	 */
	public RDFParser(final FhirContext context, final IParserErrorHandler parserErrorHandler, final Lang lang) {
		super(context, parserErrorHandler);
		this.lang = lang;
	}

	@Override
	public EncodingEnum getEncoding() {
		return EncodingEnum.RDF;
	}

	@Override
	public IParser setPrettyPrint(final boolean prettyPrint) {
		return this;
	}

	/**
	 * Writes the provided resource to the writer.  This should only be called for the top-level resource being encoded.
	 * @param resource FHIR resource for writing
	 * @param writer The writer to write to -- Note: Jena prefers streams over writers
	 * @param encodeContext encoding content from parent
	 */
	@Override
	protected void doEncodeResourceToWriter(final IBaseResource resource, final Writer writer, final EncodeContext encodeContext) {
		Model rdfModel = RDFUtil.initializeRDFModel();

		// Establish the namespaces and prefixes needed
		HashMap<String,String> prefixes = new HashMap<>();
		prefixes.put(RDF_PREFIX, RDF_NS);
		prefixes.put(RDFS_PREFIX, RDFS_NS);
		prefixes.put(XSD_PREFIX, XSD_NS);
		prefixes.put(FHIR_PREFIX, FHIR_NS);
		prefixes.put(SCT_PREFIX, SCT_NS);

		for (String key : prefixes.keySet()) {
			rdfModel.setNsPrefix(key, prefixes.get(key));
		}

		IIdType resourceId = processResourceID(resource, encodeContext);

		encodeResourceToRDFStreamWriter(resource, rdfModel, false, resourceId, encodeContext, true, null);

		RDFUtil.writeRDFModel(writer, rdfModel, lang);
	}

	/**
	 * Parses RDF content to a FHIR resource using Apache Jena
	 * @param resourceType Class of FHIR resource being deserialized
	 * @param reader Reader containing RDF (turtle) content
	 * @param <T> Type parameter denoting which resource is being parsed
	 * @return Populated FHIR resource
	 * @throws DataFormatException Exception that can be thrown from parser
	 */
	@Override
	protected <T extends IBaseResource> T doParseResource(final Class<T> resourceType, final Reader reader) throws DataFormatException {
		Model model = RDFUtil.readRDFToModel(reader, this.lang);
		return parseResource(resourceType, model);
	}

	private Resource encodeResourceToRDFStreamWriter(final IBaseResource resource,
																	 final Model rdfModel,
																	 final boolean containedResource,
																	 final IIdType resourceId,
																	 final EncodeContext encodeContext,
																	 final boolean rootResource, Resource parentResource) {

		RuntimeResourceDefinition resDef = getContext().getResourceDefinition(resource);
		if (resDef == null) {
			throw new ConfigurationException(Msg.code(1845) + "Unknown resource type: " + resource.getClass());
		}

		if (!containedResource) {
			setContainedResources(getContext().newTerser().containResources(resource));
		}

		if (!(resource instanceof IAnyResource)) {
			throw new IllegalStateException(Msg.code(1846) + "Unsupported resource found: " + resource.getClass().getName());
		}

		// Create absolute IRI for the resource
		String uriBase = resource.getIdElement().getBaseUrl();
		if (uriBase == null) {
			uriBase = getServerBaseUrl();
		}
		if (uriBase == null) {
			uriBase = FHIR_NS;
		}
		if (!uriBase.endsWith("/")) {
			uriBase = uriBase + "/";
		}

		if (parentResource == null) {
			if (!resource.getIdElement().toUnqualified().hasIdPart()) {
				parentResource = rdfModel.getResource(null);
			} else {

				String resourceUri = IRIs.resolve(uriBase, resource.getIdElement().toUnqualified().toString()).toString();
				parentResource = rdfModel.getResource(resourceUri);
			}
			// If the resource already exists and has statements, return that existing resource.
			if (parentResource != null && parentResource.listProperties().toList().size() > 0) {
				return parentResource;
			} else if (parentResource == null) {
				return null;
			}
		}

		parentResource.addProperty(RDF.type, rdfModel.createProperty(FHIR_NS + resDef.getName()));

		// Only the top-level resource should have the nodeRole set to treeRoot
		if (rootResource) {
			parentResource.addProperty(rdfModel.createProperty(FHIR_NS + NODE_ROLE), rdfModel.createProperty(FHIR_NS + TREE_ROOT));
		}

		if (resourceId != null && resourceId.getIdPart() != null) {
			parentResource.addProperty(rdfModel.createProperty(FHIR_NS + RESOURCE_ID), createFhirValueBlankNode(rdfModel, resourceId.getIdPart()));
		}

		encodeCompositeElementToStreamWriter(resource, resource, rdfModel, parentResource, containedResource, new CompositeChildElement(resDef, encodeContext), encodeContext);

		return parentResource;
	}

	/**
	 * Utility method to create a blank node with a fhir:value predicate
	 * @param rdfModel Model to create node within
	 * @param value value object - assumed to be xsd:string
	 * @return Blank node resource containing fhir:value
	 */
	private Resource createFhirValueBlankNode(Model rdfModel, String value) {
		return createFhirValueBlankNode(rdfModel, value, XSDDatatype.XSDstring, null);
	}
	/**
	 * Utility method to create a blank node with a fhir:value predicate accepting a specific data type and index
	 * @param rdfModel Model to create node within
	 * @param value value object
	 * @param xsdDataType data type for value
	 * @param cardinalityIndex if a collection, this value is written as a fhir:index predicate
	 * @return Blank node resource containing fhir:value (and possibly fhir:index)
	 */
	private Resource createFhirValueBlankNode(Model rdfModel, String value, XSDDatatype xsdDataType, Integer cardinalityIndex) {
		Resource fhirValueBlankNodeResource = rdfModel.createResource().addProperty(rdfModel.createProperty(FHIR_NS + VALUE), rdfModel.createTypedLiteral(value, xsdDataType));
		return fhirValueBlankNodeResource;
	}

	/**
	 * Builds the predicate name based on field definition
	 * @param resource Resource being interrogated
	 * @param definition field definition
	 * @param childName childName which been massaged for different data types
	 * @return String of predicate name
	 */
	private String constructPredicateName(IBaseResource resource, BaseRuntimeChildDefinition definition, String childName, IBase parentElement) {
		String basePropertyName = FHIR_NS + childName;
		String classBasedPropertyName;

		if (definition instanceof BaseRuntimeDeclaredChildDefinition) {
			BaseRuntimeDeclaredChildDefinition declaredDef = (BaseRuntimeDeclaredChildDefinition)definition;
			Class declaringClass = declaredDef.getField().getDeclaringClass();
			if (declaringClass != resource.getClass()) {
				String property = null;
				if (IBaseBackboneElement.class.isAssignableFrom(declaringClass) || IBaseDatatypeElement.class.isAssignableFrom(declaringClass)) {
					if (classToFhirTypeMap.containsKey(declaringClass)) {
						property = classToFhirTypeMap.get(declaringClass);
					} else {
						try {
							IBase elem = (IBase)declaringClass.getDeclaredConstructor().newInstance();
							property = elem.fhirType();
							classToFhirTypeMap.put(declaringClass, property);
						} catch (Exception ex) {
							logger.debug("Error instantiating an " + declaringClass.getSimpleName() + " to retrieve its FhirType");
						}
					}
				} else {
					if ("MetadataResource".equals(declaringClass.getSimpleName())) {
						property = resource.getClass().getSimpleName();
					} else {
						property = declaredDef.getField().getDeclaringClass().getSimpleName();
					}
				}
				classBasedPropertyName = FHIR_NS + childName;
				return classBasedPropertyName;
			}
		}
		return basePropertyName;
	}

	private RDFNode encodeChildElementToStreamWriter(final IBaseResource resource, IBase parentElement, Model rdfModel, Resource rdfResource,
																	  final BaseRuntimeChildDefinition childDefinition,
																	  final IBase element,
																	  final String childName,
																	  final BaseRuntimeElementDefinition<?> childDef,
																	  final boolean includedResource,
																	  final CompositeChildElement parent,
																	  final EncodeContext encodeContext, final Integer cardinalityIndex) {

		String childGenericName = childDefinition.getElementName();

		encodeContext.pushPath(childGenericName, false);
		try {

			if (element == null || element.isEmpty()) {
				if (!isChildContained(childDef, includedResource)) {
					return null;
				}
			}
			String propertyName = constructPredicateName(resource, childDefinition, childName, parentElement);
			Property propertyProperty = rdfModel.createProperty(propertyName);

			String errorContextStr = childDef.getChildType() + " " + childName + ": ";
			switch (childDef.getChildType()) {
				case ID_DATATYPE: {
					IIdType value = (IIdType) element;
					assert value != null;
					String encodedValue = ID.equals(childName) ? value.getIdPart() : value.getValue();
					if (StringUtils.isNotBlank(encodedValue) || !hasNoExtensions(value)) {
						if (StringUtils.isNotBlank(encodedValue)) {

							if (element != null) {
								XSDDatatype dataType = getXSDDataTypeForFhirType(element.fhirType(), encodedValue);
								return this.createFhirValueBlankNode(rdfModel, encodedValue, dataType, cardinalityIndex);
							}
						}
					}
					break;
				}
				case PRIMITIVE_DATATYPE: {
					IPrimitiveType<?> pd = (IPrimitiveType<?>) element;
					assert pd != null;
					String value = pd.getValueAsString();
					if (value != null || !hasNoExtensions(pd)) {
						if (value != null) {
							XSDDatatype dataType = getXSDDataTypeForFhirType(pd.fhirType(), value);
							Resource valueResource = this.createFhirValueBlankNode(rdfModel, value, dataType, cardinalityIndex);
							if (!hasNoExtensions(pd)) {
								IBaseHasExtensions hasExtension = (IBaseHasExtensions)pd;
								if (hasExtension.getExtension() != null && hasExtension.getExtension().size() > 0) {
									int i = 0;
									for (IBaseExtension extension : hasExtension.getExtension()) {
										RuntimeResourceDefinition resDef = getContext().getResourceDefinition(resource);
										Resource extensionResource = rdfModel.createResource();
										valueResource.addProperty(rdfModel.createProperty(FHIR_NS + ELEMENT_EXTENSION), extensionResource);
										encodeCompositeElementToStreamWriter(resource, extension, rdfModel, extensionResource, false, new CompositeChildElement(resDef, encodeContext), encodeContext);
									}
								}
							}

							return valueResource;
						}
					}
					break;
				}
				case RESOURCE_BLOCK:
				case COMPOSITE_DATATYPE: {
					String idString = null;
					String idPredicate = null;
					if (element instanceof IBaseResource) {
						idPredicate = FHIR_NS + RESOURCE_ID;
						IIdType resourceId = processResourceID((IBaseResource) element, encodeContext);
						if (resourceId != null) {
							idString = resourceId.getIdPart();
						}
					}
					else if (element instanceof IBaseElement) {
						idPredicate = FHIR_NS + ELEMENT_ID;
						if (((IBaseElement)element).getId() != null) {
							idString = ((IBaseElement)element).getId();
						}
					}
					if (idString != null)
						rdfResource.addProperty(rdfModel.createProperty(idPredicate), createFhirValueBlankNode(rdfModel, idString));
					encodeCompositeElementToStreamWriter(resource, element, rdfModel, rdfResource, includedResource, parent, encodeContext);
					return rdfResource;
				}
				case CONTAINED_RESOURCE_LIST:
				case CONTAINED_RESOURCES: {
					if (element == null) { throw new Error(errorContextStr + "has no element"); }
                                        if (cardinalityIndex == null) { throw new Error(errorContextStr + "expected to be in list"); }
					IIdType resourceId = ((IBaseResource)element).getIdElement();
					Resource containedResource = rdfModel.createResource();
//					rdfResource.addProperty(rdfModel.createProperty(FHIR_NS+ DOMAIN_RESOURCE_CONTAINED), containedResource);
					return encodeResourceToRDFStreamWriter((IBaseResource)element, rdfModel, true, super.fixContainedResourceId(resourceId.getValue()), encodeContext, false, containedResource);
				}
				case RESOURCE: {
					IBaseResource baseResource = (IBaseResource) element;
					String resourceName = getContext().getResourceType(baseResource);
					if (!super.shouldEncodeResource(resourceName)) {
						throw new Error(errorContextStr + "never hit"); // break;
					}
					encodeContext.pushPath(resourceName, true);
					IIdType resourceId = processResourceID(resource, encodeContext);
					encodeResourceToRDFStreamWriter(resource, rdfModel, false, resourceId, encodeContext, false, null);
					encodeContext.popPath();
					throw new Error(errorContextStr + "never hit");//					break;
				}
				case PRIMITIVE_XHTML:
				case PRIMITIVE_XHTML_HL7ORG: {
					IBaseXhtml xHtmlNode  = (IBaseXhtml)element;
					if (xHtmlNode == null) { throw new Error(errorContextStr + "element not castable to IBaseXhtml"); }
					return rdfModel.createTypedLiteral(xHtmlNode.getValueAsString());
				}
				case EXTENSION_DECLARED:
				case UNDECL_EXT:
				default: {
					throw new IllegalStateException(Msg.code(1847) + "Unexpected node - should not happen: " + childDef.getName());
				}
			}
		} finally {
			encodeContext.popPath();
		}

		return null;
	}

	/**
	 * Maps hapi internal fhirType attribute to XSDDatatype enumeration
	 * @param fhirType hapi field type
	 * @return XSDDatatype value
	 */
	private XSDDatatype getXSDDataTypeForFhirType(String fhirType, String value) {
		switch (fhirType) {
			case "boolean":
				return XSDDatatype.XSDboolean;
			case "uri":
				return XSDDatatype.XSDanyURI;
			case "decimal":
				return XSDDatatype.XSDdecimal;
			case "date":
				return XSDDatatype.XSDdate;
			case "dateTime":
			case "instant":
				switch (value.length()) { // assumes valid lexical value
					case 4:
						return XSDDatatype.XSDgYear;
					case 7:
						return XSDDatatype.XSDgYearMonth;
					case 10:
						return XSDDatatype.XSDdate;
					default:
						return XSDDatatype.XSDdateTime;
				}
			case "code":
			case "string":
			default:
				return XSDDatatype.XSDstring;
		}
	}

	private IIdType processResourceID(final IBaseResource resource, final EncodeContext encodeContext) {
		IIdType resourceId = null;

		if (StringUtils.isNotBlank(resource.getIdElement().getIdPart())) {
			resourceId = resource.getIdElement();
			if (resource.getIdElement().getValue().startsWith("urn:")) {
				resourceId = null;
			}
		}

		if (!super.shouldEncodeResourceId(resource, encodeContext)) {
			resourceId = null;
		} else if (encodeContext.getResourcePath().size() == 1 && super.getEncodeForceResourceId() != null) {
			resourceId = super.getEncodeForceResourceId();
		}

		return resourceId;
	}

	private RDFNode encodeExtension(final IBaseResource resource, Model rdfModel, Resource rdfResource,
											final boolean containedResource,
											final CompositeChildElement nextChildElem,
											final BaseRuntimeChildDefinition nextChild,
											final IBase nextValue,
											final String childName,
											final BaseRuntimeElementDefinition<?> childDef,
											final EncodeContext encodeContext, Integer cardinalityIndex) {
		BaseRuntimeDeclaredChildDefinition extDef = (BaseRuntimeDeclaredChildDefinition) nextChild;

		Resource childResource = rdfModel.createResource();
		String extensionPredicateName = constructPredicateName(resource, extDef, extDef.getElementName(), null);

		return encodeChildElementToStreamWriter(resource, null, rdfModel, childResource, nextChild, nextValue, childName,
			childDef, containedResource, nextChildElem, encodeContext, cardinalityIndex);
	}

	private Model encodeCompositeElementToStreamWriter(final IBaseResource resource,
																		final IBase element, Model rdfModel, Resource rdfResource,
																		final boolean containedResource,
																		final CompositeChildElement parent,
																		final EncodeContext encodeContext) {

		for (CompositeChildElement nextChildElem : super.compositeChildIterator(element, containedResource, parent, encodeContext)) {

			BaseRuntimeChildDefinition nextChild = nextChildElem.getDef();
			String childName = nextChildElem.getDef().getElementName();
			String propertyName = constructPredicateName(resource, nextChild, childName, element);
			Property propertyProperty = rdfModel.createProperty(propertyName);
			int maxCard = nextChild.getMax();

			if (nextChild instanceof RuntimeChildNarrativeDefinition) {
				INarrativeGenerator gen = getContext().getNarrativeGenerator();
				if (gen != null) {
					INarrative narrative;
					if (resource instanceof IResource) {
						narrative = ((IResource) resource).getText();
					} else if (resource instanceof IDomainResource) {
						narrative = ((IDomainResource) resource).getText();
					} else {
						narrative = null;
					}
					assert narrative != null;
					if (narrative.isEmpty()) {
						gen.populateResourceNarrative(getContext(), resource);
					}
					else {
						RuntimeChildNarrativeDefinition child = (RuntimeChildNarrativeDefinition) nextChild;

						// This is where we populate the parent of the narrative
						Resource childResource = rdfModel.createResource();

						String propertyName999 = constructPredicateName(resource, child, child.getElementName(), element);
						rdfResource.addProperty(rdfModel.createProperty(propertyName), childResource);

						String childName999 = nextChild.getChildNameByDatatype(child.getDatatype());
						BaseRuntimeElementDefinition<?> type = child.getChildByName(childName);
						RDFNode value = encodeChildElementToStreamWriter(resource, element,
							rdfModel, childResource, nextChild, narrative, childName, type,
							containedResource, nextChildElem, encodeContext, null);
                                                rdfResource.addProperty(propertyProperty, value);
						continue;
					}
				}
			}

			if (nextChild instanceof RuntimeChildDirectResource) {

				List<? extends IBase> values = nextChild.getAccessor().getValues(element);
				if (values == null || values.isEmpty()) {
					continue;
				}

				IBaseResource directChildResource = (IBaseResource)values.get(0);
				// If it is a direct resource, we need to create a new subject for it.
				Resource childResource = encodeResourceToRDFStreamWriter(directChildResource, rdfModel, false, directChildResource.getIdElement(), encodeContext, false, null);
				String propertyName999 = constructPredicateName(resource, nextChild, nextChild.getElementName(), element);
				rdfResource.addProperty(propertyProperty, childResource);

				continue;
			}

			if (nextChild instanceof RuntimeChildContainedResources) {
				List<? extends IBase> values = nextChild.getAccessor().getValues(element);
				if (values.size() > 0) {
					int i = 0;
					List<RDFNode> list = new ArrayList<>();
					for (IBase containedResourceEntity : values) {
						list.add(encodeChildElementToStreamWriter(resource, element, rdfModel, rdfResource, nextChild, containedResourceEntity,
							nextChild.getChildNameByDatatype(null),
							nextChild.getChildElementDefinitionByDatatype(null),
							containedResource, nextChildElem, encodeContext, i));
						i++;
					}
					rdfResource.addProperty(propertyProperty, rdfModel.createList(list.listIterator()));
				}
			} else {

				List<? extends IBase> values = nextChild.getAccessor().getValues(element);
				values = super.preProcessValues(nextChild, resource, values, nextChildElem, encodeContext);

				if (values == null || values.isEmpty()) {
					continue;
				}

				Integer cardinalityIndex = null;
				List<RDFNode> list = null;
				String listPredicate = null;
				int indexCounter = 0;

				for (IBase nextValue : values) {
					if (listPredicate != null && !listPredicate.equals(propertyName)) { // finish last list
						rdfResource.addProperty(rdfModel.createProperty(listPredicate), rdfModel.createList(list.listIterator()));
						listPredicate = null;
						list = null;
					}
					if (nextChild.getMax() != 1) {
						cardinalityIndex = indexCounter; // tail =
						indexCounter++;
					}
					if ((nextValue == null || nextValue.isEmpty())) {
						continue;
					}

					ChildNameAndDef childNameAndDef = super.getChildNameAndDef(nextChild, nextValue);
					if (childNameAndDef == null) {
						throw new Error("asdf");
					}

					BaseRuntimeElementDefinition<?> childDef = childNameAndDef.getChildDef();
					String extensionUrl = getExtensionUrl(nextChild.getExtensionUrl());

					if (extensionUrl != null && !childName.equals(EXTENSION)) {
						encodeExtension(resource, rdfModel, rdfResource, containedResource, nextChildElem, nextChild,
							nextValue, childName, childDef, encodeContext, cardinalityIndex);
					} else if (nextChild instanceof RuntimeChildExtension) {
						IBaseExtension<?, ?> extension = (IBaseExtension<?, ?>) nextValue;
						if ((extension.getValue() == null || extension.getValue().isEmpty())) {
							if (extension.getExtension().isEmpty()) {
								continue;
							}
						}
						if (cardinalityIndex != null && cardinalityIndex > -1) { // rest !!
							if (list == null) {
								listPredicate = propertyName;
								list = new ArrayList<>();
							}
						}
						RDFNode value = encodeExtension(resource, rdfModel, rdfResource, containedResource, nextChildElem, nextChild,
							nextValue, childName, childDef, encodeContext, cardinalityIndex);
						if (cardinalityIndex != null)
							list.add(value);
						else
							rdfResource.addProperty(propertyProperty, value);
					} else if (!(nextChild instanceof RuntimeChildNarrativeDefinition) || !containedResource) {

						// If the child is not a value type, create a child object (blank node) for subordinate predicates to be attached to
						if (childDef.getChildType() != PRIMITIVE_DATATYPE &&
							childDef.getChildType() != PRIMITIVE_XHTML_HL7ORG &&
							childDef.getChildType() != PRIMITIVE_XHTML &&
							childDef.getChildType() != ID_DATATYPE) {
							Resource childResource = rdfModel.createResource();

							if (cardinalityIndex != null && cardinalityIndex > -1) { // rest !!
								if (list == null) {
									listPredicate = propertyName;
									list = new ArrayList<>();
								}
							} else {
								rdfResource.addProperty(rdfModel.createProperty(propertyName), childResource);
							}
							RDFNode value = encodeChildElementToStreamWriter(resource, element, rdfModel, childResource, nextChild, nextValue,
								childName, childDef, containedResource, nextChildElem, encodeContext, cardinalityIndex);
							if (cardinalityIndex != null)
								list.add(value);
							else
								rdfResource.addProperty(propertyProperty, value);

							// e.g. valueReference
							String childChildName = childNameAndDef.getChildName();
							if (!childChildName.equals(childName) && childChildName.startsWith(childName)) { // better heuristic for polymorphic types?
								String typeStr = childChildName.substring(childName.length());
								value.asResource().addProperty(RDF.type, rdfModel.createProperty(FHIR_NS + typeStr));
							};

						}
						else {
							if (cardinalityIndex != null && cardinalityIndex > -1) { // rest !!
								if (list == null) {
									listPredicate = propertyName;
									list = new ArrayList<>();
								}
							}
							RDFNode value = encodeChildElementToStreamWriter(resource, element, rdfModel, rdfResource, nextChild, nextValue,
								childName, childDef, containedResource, nextChildElem, encodeContext, cardinalityIndex);
							if (cardinalityIndex != null)
								list.add(value);
							else
								rdfResource.addProperty(propertyProperty, value);

							// e.g. valueString
							String childChildName = childNameAndDef.getChildName();
							if (!childChildName.equals(childName) && childChildName.startsWith(childName)) { // better heuristic for polymorphic types?
								String typeStr = childChildName.substring(childName.length());
								value.asResource().addProperty(RDF.type, rdfModel.createProperty(FHIR_NS + typeStr));
							};
						}
					}
				}
				if (listPredicate != null) {
					rdfResource.addProperty(rdfModel.createProperty(listPredicate), rdfModel.createList(list.listIterator()));
					listPredicate = null;
					list = null;
				}
			}
		}
		return rdfModel;
	}

	private <T extends IBaseResource> T parseResource(Class<T> resourceType, Model rdfModel) {
		// jsonMode of true is passed in so that the xhtml parser state behaves as expected
		// Push PreResourceState
		ParserState<T> parserState = ParserState.getPreResourceInstance(this, resourceType, getContext(), true, getErrorHandler());
		return parseRootResource(rdfModel, parserState, resourceType);
	}


	private <T> T parseRootResource(Model rdfModel, ParserState<T> parserState, Class<T> resourceType) {
		logger.trace("Entering parseRootResource with state: {}", parserState);

		StmtIterator rootStatementIterator  = rdfModel.listStatements(null, rdfModel.getProperty(FHIR_NS + NODE_ROLE),  rdfModel.getProperty(FHIR_NS + TREE_ROOT));

		Resource rootResource;
		String fhirResourceType, fhirTypeString;
		while (rootStatementIterator.hasNext()) {
			Statement rootStatement = rootStatementIterator.next();
			rootResource = rootStatement.getSubject();

			// If a resourceType is not provided via the server framework, discern it based on the rdf:type Arc
			if (resourceType == null) {
				Statement resourceTypeStatement = rootResource.getProperty(RDF.type);
				fhirTypeString = resourceTypeStatement.getObject().toString();
				if (fhirTypeString.startsWith(FHIR_NS)) {
					fhirTypeString = fhirTypeString.replace(FHIR_NS, "");
				}
			} else {
				fhirTypeString = resourceType.getSimpleName();
			}

			RuntimeResourceDefinition definition = getContext().getResourceDefinition(fhirTypeString);
			fhirResourceType = definition.getName();

			parseResource(parserState, fhirResourceType, rootResource);

			// Pop PreResourceState
			parserState.endingElement();
		}
		return parserState.getObject();
	}

	private <T> void parseResource(ParserState<T> parserState, String resourceType, RDFNode rootNode) {
		// Push top-level entity
		parserState.enteringNewElement(FHIR_NS, resourceType);

		if (rootNode instanceof Resource) {
			Resource rootResource = rootNode.asResource();
			List<Statement> statements = rootResource.listProperties().toList();
			statements.sort(new FhirIndexStatementComparator());
			for (Statement statement : statements) {
				String predicateAttributeName = extractAttributeNameFromPredicate(statement);
				if (predicateAttributeName != null) {
					if (predicateAttributeName.equals(MODIFIER_EXTENSION)) {
						RDFNode statementObject = statement.getObject();
						Resource resourceObject = statementObject.asResource();
						boolean isRepeating = parserState.elementIsRepeating(predicateAttributeName);
						boolean hasRdfFirst = resourceObject.hasProperty(RDF.first);

						if (!(isRepeating == hasRdfFirst))
							System.out.println(new DataFormatException("element '" + predicateAttributeName + "'" + (isRepeating ? "" : " not") + " expected to be an RDF List"));
						List<RDFNode> objectNodes = hasRdfFirst // isRepeating
							? resourceObject.as(RDFList.class).iterator().toList()
							: Collections.singletonList(resourceObject);
						for (RDFNode objectNode : objectNodes) {
							processExtension(parserState, objectNode, true);
						}
					} else if (predicateAttributeName.equals(EXTENSION)) {
						RDFNode statementObject = statement.getObject();
						Resource resourceObject = statementObject.asResource();
						boolean isRepeating = parserState.elementIsRepeating(predicateAttributeName);
						boolean hasRdfFirst = resourceObject.hasProperty(RDF.first);

						if (!(isRepeating == hasRdfFirst))
							System.out.println(new DataFormatException("element '" + predicateAttributeName + "'" + (isRepeating ? "" : " not") + " expected to be an RDF List"));
						List<RDFNode> objectNodes = hasRdfFirst // isRepeating
							? resourceObject.as(RDFList.class).iterator().toList()
							: Collections.singletonList(resourceObject);
						for (RDFNode objectNode : objectNodes) {
							processExtension(parserState, objectNode, false);
						}
					} else {
						processStatementObject(parserState, predicateAttributeName, statement.getObject());
					}
				}
			}
		} else if (rootNode instanceof Literal) {
			parserState.attributeValue("value", rootNode.asLiteral().getString());
		}

		// Pop top-level entity
		parserState.endingElement();
	}

	private String extractAttributeNameFromPredicate(Statement statement) {
		String predicateUri = statement.getPredicate().getURI();

		// If the predicateURI is one we're ignoring, return null
		// This minimizes 'Unknown Element' warnings in the parsing process
		if (ignoredIriPredicates.contains(predicateUri) && statement.getObject().isResource() ||
			ignoredLiteralPredicates.contains(predicateUri) && statement.getObject().isLiteral()) {
			return null;
		}

		String predicateObjectAttribute = predicateUri.substring(predicateUri.lastIndexOf("/")+1);
		String predicateAttributeName;
		if (predicateObjectAttribute.contains(".")) {
			predicateAttributeName = predicateObjectAttribute.substring(predicateObjectAttribute.lastIndexOf(".")+1); // remove branch
		} else {
			predicateAttributeName = predicateObjectAttribute;
		}
		return predicateAttributeName;
	}

	private <T> void processStatementObject(ParserState<T> parserState, String predicateAttributeName, RDFNode statementObject) {
		logger.trace("Entering processStatementObject with state: {}, for attribute {}", parserState, predicateAttributeName);
		// BaseRuntimeElementDefinition<?> d = getContext().getElementDefinition(predicateAttributeName);
		if (statementObject != null) {
			if (statementObject.isLiteral()) {
				// Push attribute element
				parserState.enteringNewElement(FHIR_NS, predicateAttributeName);
				// If the object is a literal, apply the value directly
				parserState.attributeValue(VALUE, statementObject.asLiteral().getLexicalForm());

				// Pop attribute element
				parserState.endingElement();
			} else if (statementObject.isAnon()) {
				// If the object is a blank node,
				Resource resourceObject = statementObject.asResource();
				Model model = resourceObject.getModel();
				Property rdfType = model.createProperty(RDF.type.getURI());
				boolean isRepeating = parserState.elementIsRepeating(predicateAttributeName);
				boolean hasRdfFirst = resourceObject.hasProperty(RDF.first);

				if (!(isRepeating == hasRdfFirst))
					System.out.println(new DataFormatException("element '" + predicateAttributeName + "'" + (isRepeating ? "" : " not") + " expected to be an RDF List"));
				List<RDFNode> objectNodes = hasRdfFirst // isRepeating
					? resourceObject.as(RDFList.class).iterator().toList()
					: Collections.singletonList(resourceObject);
				for (RDFNode objectNode : objectNodes) {
					// Push attribute element
					List<String> datatypeStrs = resourceObject.listProperties(RDF.type).toList().stream().filter(
						s -> s.getObject().toString().startsWith(FHIR_NS)
					).map(
						s -> s.getObject().asResource().getURI().substring(FHIR_NS.length())
					).collect(Collectors.toList());
					parserState.enteringNewElement(FHIR_NS, predicateAttributeName + (datatypeStrs.size() > 0 ? datatypeStrs.get(0) : ""));
					Resource objectResource = objectNode.asResource();
					boolean containedResource = false;
					if (predicateAttributeName.equals(CONTAINED)) {
						containedResource = true;
						Statement typeStatement = objectResource.getProperty(rdfType);
						parserState.enteringNewElement(FHIR_NS, typeStatement.getObject().toString().replace(FHIR_NS, ""));
					}

					List<Statement> objectStatements = objectResource.listProperties().toList();
					objectStatements.sort(new FhirIndexStatementComparator());
					for (Statement objectProperty : objectStatements) {
						if (objectProperty.getPredicate().hasURI(FHIR_NS + VALUE)) {
							String nestedAttributeName = "value";
							parserState.attributeValue(nestedAttributeName, objectProperty.getObject().asLiteral().getLexicalForm());
						} else {
							// Otherwise, process it as a net-new node
							String nestedAttributeName = extractAttributeNameFromPredicate(objectProperty);
							if (nestedAttributeName != null) {
								if (nestedAttributeName.equals(EXTENSION)) {
									List<RDFNode> extensionNodes = objectProperty.getObject().asResource().hasProperty(RDF.first) // TODO: sometimes a list; why?
										? objectProperty.getObject().as(RDFList.class).iterator().toList()
										: Collections.singletonList(objectProperty.getObject());
									for (RDFNode extensionNode : extensionNodes) {
										processExtension(parserState, extensionNode, false);
									}
								} else if (nestedAttributeName.equals(MODIFIER_EXTENSION)) {
									List<RDFNode> extensionNodes = objectProperty.getObject().asResource().hasProperty(RDF.first)
										? objectProperty.getObject().as(RDFList.class).iterator().toList()
										: Collections.singletonList(objectProperty.getObject());
									for (RDFNode extensionNode : extensionNodes) {
										processExtension(parserState, extensionNode, true);
									}
								} else {
									processStatementObject(parserState, nestedAttributeName, objectProperty.getObject());
								}
							}
						}
					}

					if (containedResource) {
						// Leave the contained resource element we created
						parserState.endingElement();
					}

					// Pop attribute element
					parserState.endingElement();
				}
			} else if (statementObject.isResource()) {
				if (statementObject.asResource().getURI().equals(RDF.nil.getURI())) {
					throw new DataFormatException("value of `" + predicateAttributeName + "` should not be an empty list");
				}
				// Push attribute element
				parserState.enteringNewElement(FHIR_NS, predicateAttributeName);
				Resource innerResource = statementObject.asResource();
				Statement resourceTypeStatement = innerResource.getProperty(RDF.type);
				String fhirTypeString = resourceTypeStatement.getObject().toString();
				if (fhirTypeString.startsWith(FHIR_NS)) {
					fhirTypeString = fhirTypeString.replace(FHIR_NS, "");
				}
				parseResource(parserState, fhirTypeString, innerResource);

				// Pop attribute element
				parserState.endingElement();
			}
		}
	}

	private <T> void processExtension(ParserState<T> parserState, RDFNode statementObject, boolean isModifier) {
		logger.trace("Entering processExtension with state: {}", parserState);
		Resource resource = statementObject.asResource();
		Statement urlProperty = resource.getProperty(resource.getModel().createProperty(FHIR_NS+EXTENSION_URL));
		Resource urlPropertyResource = urlProperty.getObject().asResource();
		String extensionUrl = urlPropertyResource.getProperty(resource.getModel().createProperty(FHIR_NS+VALUE)).getObject().asLiteral().getString();

		List<Statement> extensionStatements = resource.listProperties().toList();
		String extensionValueType = null;
		RDFNode extensionValueResource = null;
		for (Statement statement : extensionStatements) {
			String propertyUri = statement.getPredicate().getURI();
			if (propertyUri.contains("value")) {
				List<String> datatypeStrs = statement.getObject().asResource().listProperties(RDF.type).toList().stream().filter(
					s -> s.getObject().toString().startsWith(FHIR_NS)
				).map(
					s -> s.getObject().asResource().getURI().substring(FHIR_NS.length())
				).collect(Collectors.toList());
//				extensionValueType = propertyUri.replace(FHIR_NS, "") + datatypeStrs.get(0);
				extensionValueType = "value" + datatypeStrs.get(0);
				BaseRuntimeElementDefinition<?> target = getContext().getRuntimeChildUndeclaredExtensionDefinition().getChildByName(extensionValueType);
				if (target.getChildType().equals(ID_DATATYPE) || target.getChildType().equals(PRIMITIVE_DATATYPE)) {
					extensionValueResource = statement.getObject().asResource().getProperty(resource.getModel().createProperty(FHIR_NS+VALUE)).getObject().asLiteral();
				} else {
					extensionValueResource = statement.getObject().asResource();
				}
				break;
			}
		}

		parserState.enteringNewElementExtension(null, extensionUrl, isModifier, null);
		// Some extensions don't have their own values - they then have more extensions inside of them
		if (extensionValueType != null) {
			parseResource(parserState, extensionValueType, extensionValueResource);
		}

		for (Statement statement : extensionStatements) {
			String propertyUri = statement.getPredicate().getURI();
			if (propertyUri.equals(FHIR_NS + ELEMENT_EXTENSION)) {
				String predicateAttributeName = extractAttributeNameFromPredicate(statement);
				RDFNode extensionObject = statement.getObject();
				Resource resourceObject = extensionObject.asResource();
				boolean isRepeating = parserState.elementIsRepeating(predicateAttributeName);
				boolean hasRdfFirst = resourceObject.hasProperty(RDF.first);

				if (!(isRepeating == hasRdfFirst))
					System.out.println(new DataFormatException("element '" + predicateAttributeName + "'" + (isRepeating ? "" : " not") + " expected to be an RDF List"));
				List<RDFNode> objectNodes = hasRdfFirst // isRepeating
					? resourceObject.as(RDFList.class).iterator().toList()
					: Collections.singletonList(resourceObject);
				for (RDFNode objectNode : objectNodes) {
					processExtension(parserState, objectNode, false);
				}
			}
		}

		parserState.endingElement();
	}

	static class FhirIndexStatementComparator implements Comparator<Statement> {

		@Override
		public int compare(Statement arg0, Statement arg1) {
			int result = arg0.getPredicate().getURI().compareTo(arg1.getPredicate().getURI());
			if (result == 0) {
				if (arg0.getObject().isResource() && arg1.getObject().isResource()) {
					Resource resource0 = arg0.getObject().asResource();
					Resource resource1 = arg1.getObject().asResource();

					result = Integer.compare(getFhirIndex(resource0), getFhirIndex(resource1));
				}

			}
			return result;
		}

		private int getFhirIndex(Resource resource) {
			if (resource.hasProperty(resource.getModel().createProperty(FHIR_NS+FHIR_INDEX))) {if (true) throw new Error("vestigial fhir:index");
				StmtIterator it = resource.listProperties(resource.getModel().createProperty(FHIR_NS+FHIR_INDEX));
				while (it.hasNext()) {
					RDFNode o = it.nextStatement().getObject();
					if (o.isLiteral())
						return o.asLiteral().getInt();
				}
			}
			return -1;
		}
	}
}
