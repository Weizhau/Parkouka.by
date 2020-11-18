package weizhau.parkoukaby.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import weizhau.parkoukaby.FragmentRelations
import weizhau.parkoukaby.R

class FirstLaunchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_start, container, false)

        val viewPager = view.findViewById<ViewPager2>(R.id.start_input_viewpager)

        val pagerAdapter = PagerAdapter(fragmentManager!!, lifecycle)
        pagerAdapter.addFragment(InputPhoneFragment(viewPager))
        pagerAdapter.addFragment(InputNumbersFragment(activity as FragmentRelations.Waiter))

        viewPager.adapter = pagerAdapter

        return view
    }

    class PagerAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fm, lifecycle) {
        val fragmentList = ArrayList<Fragment>()

        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }

        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList.get(position)
        }
    }
}