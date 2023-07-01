plugins {
    id("fr.stardustenterprises.rust.wrapper")
}

val nativeName = "test"

rust {
    release.set(true)
    command.set("cargo")
    cargoInstallTargets.set(true)

    targets {
        this += defaultTarget()

        create("linux64") {
            target = "x86_64-unknown-linux-gnu"
            outputName = "lib${nativeName}.so"
        }

        create("win64") {
            target = "x86_64-pc-windows-gnu"
            outputName = "${nativeName}.dll"
        }

        create("macOS-x86") {
            target = "x86_64-apple-darwin"
            outputName = "lib${nativeName}.dylib"

            command = "cargo"
            env += "CC" to "oa64-clang"
            env += "CXX" to "oa64-clang++"
        }

        create("macOS-aarch64") {
            target = "aarch64-apple-darwin"
            outputName = "lib${nativeName}.dylib"

            command = "cargo"
            env += "CC" to "oa64-clang"
            env += "CXX" to "oa64-clang++"
        }
    }
}
