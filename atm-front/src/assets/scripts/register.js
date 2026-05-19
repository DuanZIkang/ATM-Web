import axios from "axios";
import NavBar from "@/components/NavBar.vue";

export default {
    components: { NavBar },

    data() {
        return {
            name: "",
            password: "",
            confirm: "",
            sex: "",
            msg: ""
        };
    },

    methods: {
        async submitRegister() {
            this.msg = "";

            // 必填项校验
            if (!this.name || !this.password || !this.confirm || !this.sex) {
                this.msg = "请填写所有字段";
                return;
            }

            if (this.password !== this.confirm) {
                this.msg = "两次密码不一致";
                return;
            }

            try {
                // 不传 card，由后端自动生成
                const res = await axios.post(
                    `${process.env.VUE_APP_API_URL}/register`,
                    {
                        name: this.name,
                        password: this.password,
                        balance: 0,
                        limit: 20000,
                        sex: this.sex
                    }
                );

                const cardNumber = res.data.data.card;

                // 弹窗提示卡号
                alert(
                    `开户成功！您的卡号是：${cardNumber}\n\n` +
                    `请妥善保管，登录时需要使用此卡号。\n点击确定进入首页。`
                );

                this.$router.push("/home");

            } catch (e) {
                this.msg = e.response?.data || "服务器错误";
            }
        }
    }
};
