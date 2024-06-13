import {
  Button,
  Flex,
  Group,
  MultiSelect,
  Select,
  Slider,
  Text,
} from '@mantine/core';
import {
  languages,
  priceMarks,
  ratingMarks,
  useFilters,
  weekDays,
} from '../hooks';
import {
  IconAdjustments,
  IconCalendar,
  IconCurrencyEuro,
  IconLanguage,
  // IconLocation,
  IconStar,
} from '@tabler/icons-react';

interface FiltersProps {
  onSubmit?: () => void;
}

export function Filters(props: FiltersProps) {
  const { onSubmit } = props;
  const { filters, handleChange } = useFilters();

  return (
    <Group>
      <Flex justify='center' align='center' gap={1}>
        <Text size='lg' fw={500} visibleFrom='sm'>
          Filters
        </Text>
        <Group visibleFrom='sm'>
          <IconAdjustments size={20} stroke={1} />
        </Group>
      </Flex>

      {/* <Group>
        <TextInput
          label={
            <Flex justify='center' align='center'>
              <Text className='text-sm'>Location</Text>
              <IconLocation size={14} />
            </Flex>
          }
          name='location'
          placeholder='Enter location'
          className='w-full'
          value={filters.location}
          onChange={(event) =>
            handleChange('location')(event.currentTarget.value)
          }
        />
      </Group> */}

      <Group className='w-full mb-4 mr-1'>
        <Flex justify='center' align='center'>
          <Text className='text-sm'>Min Ratings</Text>
          <IconStar size={14} />
        </Flex>
        <Slider
          name='ratingsMin'
          min={1}
          max={5}
          step={1}
          className='w-full'
          marks={ratingMarks}
          value={filters.ratingsMin}
          onChange={handleChange('ratingsMin')}
        />
      </Group>

      <Group className='w-full mb-4 mr-1'>
        <Flex justify='center' align='center'>
          <Text className='text-sm'>Max Price</Text>
          <IconCurrencyEuro size={14} />
        </Flex>
        <Slider
          name='pricingMax'
          marks={priceMarks}
          min={12}
          max={100}
          className='w-full'
          value={filters.pricingMax}
          onChange={handleChange('pricingMax')}
        />
      </Group>

      <Group className='mb-1 w-full'>
        <MultiSelect
          name='availabilityDays'
          label={
            <Flex justify='center' align='center'>
              <Text className='text-sm'>Availibity Days</Text>
              <IconCalendar size={14} />
            </Flex>
          }
          placeholder='Select days'
          className='w-full'
          data={weekDays}
          value={filters.availabilityDays}
          onChange={handleChange('availabilityDays')}
        />
      </Group>

      <Group className='mb-1'>
        <Select
          name='language'
          label=<Flex justify='center' align='center'>
            <Text className='text-sm'>Langauge</Text>
            <IconLanguage size={14} />
          </Flex>
          placeholder='Select language'
          className='w-full'
          data={languages}
          value={filters.language}
          onChange={handleChange('language')}
        />
      </Group>

      <Group className='w-full mb-4 mr-1' hiddenFrom='sm'>
        <Button onClick={onSubmit}>Apply Filters</Button>
      </Group>
    </Group>
  );
}