import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.kotlin.databinding.DialogCustomBinding


class CommonDialog : DialogFragment() {
    private var _binding: DialogCustomBinding? = null
    private val binding get() = _binding!!

    // 定义按钮点击回调
    var onPositiveClick: (() -> Unit)? = null
    var onNegativeClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCustomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置对话框内容
        setupDialogContent()

        // 设置按钮点击事件
        binding.btnPositive.setOnClickListener {
            onPositiveClick?.invoke()
            dismiss()
        }

        binding.btnNegative.setOnClickListener {
            onNegativeClick?.invoke()
            dismiss()
        }
    }

    private fun setupDialogContent() {
        arguments?.let { args ->
            // 设置标题
            args.getString("title")?.let { title ->
                binding.tvTitle.text = title
            }

            // 设置消息内容
            args.getString("message")?.let { message ->
                binding.tvMessage.text = message
            }

            // 设置按钮文本
            args.getString("positiveText")?.let { positiveText ->
                binding.btnPositive.text = positiveText
            }

            args.getString("negativeText")?.let { negativeText ->
                binding.btnNegative.text = negativeText
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            title: String,
            message: String,
            positiveText: String = "确定",
            negativeText: String = "取消"
        ): CommonDialog {
            return CommonDialog().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("message", message)
                    putString("positiveText", positiveText)
                    putString("negativeText", negativeText)
                }
            }
        }
    }
}