package com.bardurt.openmapview

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment


class SnapshotFragment : DialogFragment() {

    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap

    @Suppress("deprecation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARGUMENT_BITMAP, Bitmap::class.java)!!
            } else {
                it.getParcelable(ARGUMENT_BITMAP)!!
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_snapshot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById(R.id.imageViewSnapshot)
        imageView.setImageBitmap(bitmap)
    }

    companion object {

        const val TAG = "SnapshotFragment"

        private const val ARGUMENT_BITMAP = "bitmap"

        @JvmStatic
        fun newInstance(bitmap: Bitmap) =
            SnapshotFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARGUMENT_BITMAP, bitmap)
                }
            }
    }
}