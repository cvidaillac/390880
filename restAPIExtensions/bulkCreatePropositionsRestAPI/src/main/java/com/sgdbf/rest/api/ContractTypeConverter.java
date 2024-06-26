package com.sgdbf.rest.api;

import static org.bonitasoft.engine.bpm.contract.InputDefinition.FILE_INPUT_ID;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
//import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
//import org.bonitasoft.engine.bpm.document.DocumentException;

public class ContractTypeConverter {
	   protected static final Logger LOGGER = Logger.getLogger(ContractTypeConverter.class.getName());

	    public static final String[] ISO_8601_DATE_PATTERNS = new String[] { "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss'Z'",
	            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" };

	    static final String CONTENT_TYPE = "contentType";
	    static final String FILE_TEMP_PATH = "tempPath";
	    static final String TEMP_PATH_DESCRIPTION = "file name in the temporary upload directory";

	    private final ConvertUtilsBean convertUtilsBean;

	    public ContractTypeConverter(final String[] datePatterns) {
	        convertUtilsBean = new ConvertUtilsBean();
	        convertUtilsBean.register(true, false, 0);
	        final DateConverter dateConverter = new DateConverter();
	        dateConverter.setPatterns(datePatterns);
	        dateConverter.setTimeZone(TimeZone.getTimeZone("GMT"));
	        convertUtilsBean.register(dateConverter, Date.class);
	    }

	    Object convertToType(final Type type, final Serializable parameterValue) {
	        final Class<? extends Serializable> clazz = getClassFromType(type);
	        Serializable preprocessedParameterValue = preprocessInputs(type, parameterValue);
	        return convertToType(clazz, preprocessedParameterValue);
	    }

	    private Serializable preprocessInputs(final Type type, final Serializable parameterValue) {
	        //Also support Integer as DATE contract input (as deserialization is handled by jackson it can be mapped to an integer instead of a long when it is a small number)
	        if (Type.DATE.equals(type) && parameterValue instanceof Integer) {
	            return Long.valueOf((Integer) parameterValue);
	        }
	        return parameterValue;
	    }

	    public Map<String, Serializable> getProcessedInput(final ContractDefinition processContract, final Map<String, Serializable> inputs) throws FileNotFoundException {
	        final Map<String, Serializable> processedInputs = new HashMap<>();
	        final Map<String, Serializable> contractDefinitionMap = processContract == null ? Collections.<String, Serializable> emptyMap()
	                : createContractInputMap(processContract.getInputs());
	        if (inputs != null) {
	            for (final Entry<String, Serializable> inputEntry : inputs.entrySet()) {
	                processedInputs.put(inputEntry.getKey(),
	                        convertInputToExpectedType(inputEntry.getValue(),
	                                contractDefinitionMap.get(inputEntry.getKey())));
	            }
	        }
	        return processedInputs;
	    }

	    private Serializable convertInputToExpectedType(final Serializable inputValue, final Serializable inputDefinition)
	            throws FileNotFoundException {
	        if (inputValue == null) {
	            return null;
	        } else if (inputValue instanceof List) {
	            return convertMultipleInputToExpectedType(inputValue, inputDefinition);
	        } else {
	            return convertSingleInputToExpectedType(inputValue, inputDefinition);
	        }
	    }

	    private Serializable convertMultipleInputToExpectedType(final Serializable inputValue,
	            final Serializable inputDefinition)
	            throws FileNotFoundException {
	        @SuppressWarnings("unchecked")
	        final List<Serializable> listOfValues = (List<Serializable>) inputValue;
	        final List<Serializable> convertedListOfValues = new ArrayList<>();
	        for (final Serializable value : listOfValues) {
	            Serializable convertedValue = null;
	            if (value != null) {
	                convertedValue = convertSingleInputToExpectedType(value, inputDefinition);
	            }
	            convertedListOfValues.add(convertedValue);
	        }
	        return (Serializable) convertedListOfValues;
	    }

	    private Serializable convertSingleInputToExpectedType(final Serializable inputValue,
	            final Serializable inputDefinition)
	            throws FileNotFoundException {
	        if (inputDefinition == null) {
	            return inputValue;
	        } else if (inputDefinition instanceof Map) {
	            @SuppressWarnings("unchecked")
	            final Map<String, Serializable> mapOfInputDefinition = (Map<String, Serializable>) inputDefinition;
	            return convertComplexInputToExpectedType(inputValue, mapOfInputDefinition);
	        } else {
	            final InputDefinition simpleInputDefinition = (InputDefinition) inputDefinition;
	            if (Type.FILE.equals(simpleInputDefinition.getType())) {
	                // Not supported
	            	//return convertFileInputToExpectedType(inputValue);
	            	return null;
	            } else {
	                return (Serializable) convertToType(simpleInputDefinition.getType(), inputValue);
	            }
	        }
	    }

	    private Serializable convertComplexInputToExpectedType(final Serializable inputValue,
	            final Map<String, Serializable> mapOfInputDefinition) throws FileNotFoundException {
	        if (inputValue instanceof Map) {
	            @SuppressWarnings("unchecked")
	            final Map<String, Serializable> mapOfValues = (Map<String, Serializable>) inputValue;
	            final Map<String, Serializable> convertedMapOfValues = new HashMap<>();
	            for (final Entry<String, Serializable> valueEntry : mapOfValues.entrySet()) {
	                final Serializable childInputDefinition = mapOfInputDefinition.get(valueEntry.getKey());
	                final Serializable convertedValue = convertInputToExpectedType(valueEntry.getValue(),
	                        childInputDefinition);
	                convertedMapOfValues.put(valueEntry.getKey(), convertedValue);
	            }
	            return (Serializable) convertedMapOfValues;
	        } else {
	            return inputValue;
	        }
	    }

	    private Map<String, Serializable> createContractInputMap(final List<InputDefinition> inputDefinitions) {
	        final Map<String, Serializable> contractDefinitionMap = new HashMap<>();
	        for (final InputDefinition inputDefinition : inputDefinitions) {
	            if (inputDefinition.hasChildren() && !Type.FILE.equals(inputDefinition.getType())) {
	                contractDefinitionMap.put(inputDefinition.getName(),
	                        (Serializable) createContractInputMap(inputDefinition.getInputs()));
	            } else {
	                contractDefinitionMap.put(inputDefinition.getName(), inputDefinition);
	            }
	        }
	        return contractDefinitionMap;
	    }

	    public ContractDefinition getAdaptedContractDefinition(final ContractDefinition contract) {
	        final List<ConstraintDefinition> constraints = contract.getConstraints();
	        final List<InputDefinition> inputDefinitions = adaptContractInputList(contract.getInputs());
	        final ContractDefinitionImpl contractDefinition = getContractDefinition(constraints, inputDefinitions);
	        return contractDefinition;
	    }

	    private List<InputDefinition> adaptContractInputList(final List<InputDefinition> inputDefinitions) {
	        final List<InputDefinition> contractDefinition = new ArrayList<>();
	        for (final InputDefinition inputDefinition : inputDefinitions) {
	            List<InputDefinition> childInputDefinitions;
	            if (Type.FILE.equals(inputDefinition.getType())) {
	                childInputDefinitions = getFileChildInputDefinitions(inputDefinition);
	            } else if (inputDefinition.hasChildren()) {
	                childInputDefinitions = adaptContractInputList(inputDefinition.getInputs());
	            } else {
	                childInputDefinitions = new ArrayList<>();
	            }
	            final InputDefinition newInputDefinition = new InputDefinitionImpl(inputDefinition.getName(), inputDefinition.getDescription(),
	                    inputDefinition.isMultiple(), inputDefinition.getType(), childInputDefinitions);
	            contractDefinition.add(newInputDefinition);
	        }
	        return contractDefinition;
	    }

	    private List<InputDefinition> getFileChildInputDefinitions(final InputDefinition inputDefinition) {
	        List<InputDefinition> childInputDefinitions;
	        childInputDefinitions = new ArrayList<>();
	        for (final InputDefinition childInputDefinition : inputDefinition.getInputs()) {
	            if (Type.BYTE_ARRAY.equals(childInputDefinition.getType())) {
	                childInputDefinitions.add(new InputDefinitionImpl(FILE_TEMP_PATH, TEMP_PATH_DESCRIPTION, false, Type.TEXT, new ArrayList<InputDefinition>()));
	            } else {
	                childInputDefinitions.add(childInputDefinition);
	            }
	        }
	        return childInputDefinitions;
	    }

	    private ContractDefinitionImpl getContractDefinition(final List<ConstraintDefinition> constraints, final List<InputDefinition> inputDefinitions) {
	        final ContractDefinitionImpl contractDefinition = new ContractDefinitionImpl();
	        for (final ConstraintDefinition constraint : constraints) {
	            contractDefinition.addConstraint(constraint);
	        }

	        for (final InputDefinition input : inputDefinitions) {
	            contractDefinition.addInput(input);
	        }
	        return contractDefinition;
	    }

	    private Object convertToType(final Class<? extends Serializable> clazz, final Serializable parameterValue) {
	        if (parameterValue == null) {
	            return null;
	        }
	        String paramValueString = parameterValue.toString();
	        try {
	            if (clazz == LocalDate.class) {
	                //We drop useless info received from the widget ex: 2010-12-04T18:42:10Z, we drop T18:42:10Z to allow conversion
	                if (paramValueString.length() > 10) {
	                    logMessage(Level.FINE, "The string " + paramValueString
	                            + " contains information that will be dropped to convert it to a LocalDate (most likely time and timezone information which are not relevant).");
	                    paramValueString = paramValueString.substring(0, 10);
	                }
	                return LocalDate.parse(paramValueString);
	            } else if (clazz == LocalDateTime.class) {
	                try {
	                    return LocalDateTime.parse(paramValueString);
	                } catch (DateTimeParseException e) {
	                    logMessage(Level.FINE, "The string " + paramValueString
	                            + " contains information that will be dropped to convert it to a LocalDateTime (most likely time and timezone information which are not relevant).");
	                    //We drop the timezone info from the String:
	                    return ZonedDateTime.parse(paramValueString).toLocalDateTime();
	                }
	            } else if (clazz == OffsetDateTime.class) {
	                ZonedDateTime zonedDateTime = ZonedDateTime.parse(paramValueString);
	                return zonedDateTime.toOffsetDateTime();
	            } else {
	                return convertUtilsBean.convert(parameterValue, clazz);
	            }
	        } catch (final Exception e) {
	            logMessage(Level.INFO, "unable to parse '" + parameterValue + "' to type " + clazz.getName(), e);
	            return parameterValue;
	        }
	    }

	    void logMessage(Level level, String msg) {
	        LOGGER.log(level, msg);
	    }

	    void logMessage(Level level, String msg, Throwable th) {
	        LOGGER.log(level, msg, th);
	    }

	    private Class<? extends Serializable> getClassFromType(final Type type) {
	        switch (type) {
	            case BOOLEAN:
	                return Boolean.class;
	            case DATE:
	                return Date.class;
	            case INTEGER:
	                return Integer.class;
	            case DECIMAL:
	                return Double.class;
	            case BYTE_ARRAY:
	                return Byte[].class;
	            case LONG:
	                return Long.class;
	            case LOCALDATE:
	                return LocalDate.class;
	            case LOCALDATETIME:
	                return LocalDateTime.class;
	            case OFFSETDATETIME:
	                return OffsetDateTime.class;
	            default:
	                return String.class;
	        }
	    }
}
