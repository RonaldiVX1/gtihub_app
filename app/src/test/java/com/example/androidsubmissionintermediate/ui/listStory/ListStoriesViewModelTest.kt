package com.example.androidsubmissionintermediate.ui.listStory

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.androidsubmissionintermediate.data.StoryRepository
import com.example.androidsubmissionintermediate.data.response.story.Story
import com.example.androidsubmissionintermediate.utils.DataDummy
import com.example.androidsubmissionintermediate.utils.MainDispatcherRule
import com.example.androidsubmissionintermediate.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ListStoryViewModelTest{

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock private lateinit var storyRepository: StoryRepository

    private val dummyStoriesResponse = DataDummy.generateDummyStories()

    @Test
    fun `when getStories Should Not Null and Return Success`() = runTest {
        val data: PagingData<Story> = StoryPagingSource.snapshot(dummyStoriesResponse.listStory)
        val expectedStories = MutableLiveData<PagingData<Story>>()
        expectedStories.value = data
        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedStories)

        val listStoryViewModel = ListStoriesViewModel(storyRepository)
        val actualStories: PagingData<Story> = listStoryViewModel.story.getOrAwaitValue()

        val pagingDiffer = AsyncPagingDataDiffer(
            diffCallback = ListStoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        pagingDiffer.submitData(actualStories)

        Assert.assertNotNull(pagingDiffer.snapshot())
        Assert.assertEquals(dummyStoriesResponse.listStory, pagingDiffer.snapshot())
        Assert.assertEquals(dummyStoriesResponse.listStory.size, pagingDiffer.snapshot().size)
        Assert.assertEquals(dummyStoriesResponse.listStory[0], pagingDiffer.snapshot()[0])

    }


    @Test
    fun `when getStories Should Be Null and Return Success`() = runTest {

        val expectedStories = MutableLiveData<PagingData<Story>>()
        expectedStories.value = PagingData.empty()
        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedStories)

        val listStoryViewModel = ListStoriesViewModel(storyRepository)
        val actualStories: PagingData<Story> = listStoryViewModel.story.getOrAwaitValue()

        val pagingDiffer = AsyncPagingDataDiffer(
            diffCallback = ListStoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        pagingDiffer.submitData(actualStories)

        Assert.assertNotNull(pagingDiffer.snapshot())
        Assert.assertTrue(pagingDiffer.snapshot().isEmpty())
    }
}

class StoryPagingSource : PagingSource<Int, LiveData<List<Story>>>() {
    companion object {
        fun snapshot(items: List<Story>): PagingData<Story> {
            return PagingData.from(items)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, LiveData<List<Story>>>): Int {
        return 0
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<Story>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}

