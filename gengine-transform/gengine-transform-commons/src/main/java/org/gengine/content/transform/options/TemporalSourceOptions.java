package org.gengine.content.transform.options;

import java.io.Serializable;
import java.util.Map;

import org.gengine.content.mediatype.FileMediaType;
import org.gengine.content.transform.options.AbstractTransformationSourceOptions;
import org.gengine.error.GengineRuntimeException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Time-based content conversion options to specify an offset and duration.
 * Useful for audio and video.
 * <p>
 * If only the offset is specified transformers should attempt
 * a transform from that offset to the end if possible.
 * <p>
 * If only a duration is specified transformers should attempt
 * a transform from the start until that duration is reached if possible.
 *
 */
public class TemporalSourceOptions extends AbstractTransformationSourceOptions
{
    private static final long serialVersionUID = 7255206141096239901L;

    /** Validation regex for hh:mm:ss[.xxx], ignoring leap seconds and allowing up to 99 hours */
    private static final String VALID_TIME_STRING_REGEX = "\\d{2}:[0-5][0-9]:[0-5][0-9](\\.\\d{1,3})?";

    /** The offset time code from which to start the transformation */
    private String offset;

    /** The duration of the target video after the transformation */
    private String duration;

    @Override
    public boolean isApplicableForMediaType(String sourceMimetype)
    {
        return ((sourceMimetype != null &&
                sourceMimetype.startsWith(FileMediaType.PREFIX_VIDEO) ||
                sourceMimetype.startsWith(FileMediaType.PREFIX_AUDIO)) ||
                super.isApplicableForMediaType(sourceMimetype));
    }

    /**
     * Gets the offset time code from which to start the transformation
     * with a format of hh:mm:ss[.xxx]
     *
     * @return the offset
     */
    public String getOffset()
    {
        return offset;
    }

    /**
     * Sets the offset time code from which to start the transformation
     * with a format of hh:mm:ss[.xxx]
     *
     * @param offset
     */
    public void setOffset(String offset)
    {
        TemporalSourceOptions.validateTimeString(offset);
        this.offset = offset;
    }

    /**
     * Gets the duration of the source to read
     * with a format of hh:mm:ss[.xxx]
     *
     * @return
     */
    public String getDuration()
    {
        return duration;
    }

    /**
     * Sets the duration of the source to read
     * with a format of hh:mm:ss[.xxx]
     *
     * @param duration
     */
    public void setDuration(String duration)
    {
        TemporalSourceOptions.validateTimeString(duration);
        this.duration = duration;
    }

    /**
     * Validates that the given value is of the form hh:mm:ss[.xxx]
     *
     * @param value
     */
    public static void validateTimeString(String value)
    {
        if (value != null && !value.matches(VALID_TIME_STRING_REGEX))
        {
            throw new GengineRuntimeException("'" + value + "' is not a valid time specification of the form hh:mm:ss[.xxx]");
        }
    }

    @Override
    public TransformationSourceOptions mergedOptions(TransformationSourceOptions overridingOptions)
    {
        if (overridingOptions instanceof TemporalSourceOptions)
        {
            TemporalSourceOptions mergedOptions = (TemporalSourceOptions) super.mergedOptions(overridingOptions);

            if (((TemporalSourceOptions) overridingOptions).getOffset() != null)
            {
                mergedOptions.setOffset(((TemporalSourceOptions) overridingOptions).getOffset());
            }
            if (((TemporalSourceOptions) overridingOptions).getDuration() != null)
            {
                mergedOptions.setDuration(((TemporalSourceOptions) overridingOptions).getDuration());
            }
            return mergedOptions;
        }
        return null;
    }

    @Override
    @JsonIgnore
    public TransformationSourceOptionsSerializer getSerializer()
    {
        return TemporalSourceOptions.createSerializerInstance();
    }

    /**
     * Creates an instance of the options serializer
     *
     * @return the options serializer
     */
    public static TransformationSourceOptionsSerializer createSerializerInstance()
    {
        return (new TemporalSourceOptions()).new TemporalSourceOptionsSerializer();
    }

    /**
     * Serializer for temporal source options
     */
    public class TemporalSourceOptionsSerializer implements TransformationSourceOptionsSerializer
    {
        public static final String PARAM_SOURCE_TIME_OFFSET = "source_time_offset";
        public static final String PARAM_SOURCE_TIME_DURATION = "source_time_duration";

        @Override
        public TransformationSourceOptions deserialize(SerializedTransformationOptionsAccessor serializedOptions)
        {
            String offset = serializedOptions.getCheckedParam(PARAM_SOURCE_TIME_OFFSET, String.class);
            String duration = serializedOptions.getCheckedParam(PARAM_SOURCE_TIME_DURATION, String.class);

            if (offset == null && duration == null)
            {
                return null;
            }

            TemporalSourceOptions sourceOptions = new TemporalSourceOptions();
            sourceOptions.setOffset(offset);
            sourceOptions.setDuration(duration);
            return sourceOptions;
        }

        @Override
        public void serialize(TransformationSourceOptions sourceOptions,
                Map<String, Serializable> parameters)
        {
            if (parameters == null || sourceOptions == null)
                return;
            TemporalSourceOptions temporalSourceOptions = (TemporalSourceOptions) sourceOptions;
            parameters.put(PARAM_SOURCE_TIME_OFFSET, temporalSourceOptions.getOffset());
            parameters.put(PARAM_SOURCE_TIME_DURATION, temporalSourceOptions.getDuration());
        }
    }


}
