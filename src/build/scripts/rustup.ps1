$ErrorActionPreference = "Stop"

if ([environment]::Is64BitOperatingSystem) {
    $arch = "x86_64"
    $install_dir = "C:\Program Files\Rust"
} else {
    $arch = "i686"
    $install_dir = "C:\Program Files (x86)\Rust"
}

$package = "rust-nightly-$($arch)-pc-windows-gnu.exe"
$url = "https://static.rust-lang.org/dist/$($package)"

echo "Downloading Rust and Cargo from $($url)"
Start-FileDownload $url

echo "Installing Rust"
Start-Process ".\$($package)" -ArgumentList "/VERYSILENT /NORESTART" -NoNewWindow -Wait

echo "Refreshing Path"
$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "User") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "Machine")

echo "Rust and Cargo are ready to roll!"
